package com.spring.social.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.facebook.api.Device;
import org.springframework.social.facebook.api.EducationExperience;
import org.springframework.social.facebook.api.Experience;
import org.springframework.social.facebook.api.GraphApi;
import org.springframework.social.facebook.api.ImageType;
import org.springframework.social.facebook.api.PlaceTag;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.WorkEntry;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.api.plus.Person;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.support.URIBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.social.account.constants.UserAPIConstants;
import com.spring.social.account.dao.AdditionalDetailDao;
import com.spring.social.account.dao.FBDeviceInfoDao;
import com.spring.social.account.dao.FBEducationalDao;
import com.spring.social.account.dao.FBHomeTownDao;
import com.spring.social.account.dao.FBInspiratinalDao;
import com.spring.social.account.dao.FBMutualFriendsDao;
import com.spring.social.account.dao.FBMutualLikesDao;
import com.spring.social.account.dao.FBSportsDao;
import com.spring.social.account.dao.FbFavouriteAthleteDao;
import com.spring.social.account.dao.FbFavouriteTeamDao;
import com.spring.social.account.dao.TaggedPlaceDao;
import com.spring.social.account.dao.UserDetailDao;
import com.spring.social.account.dao.WorkEntryDao;
import com.spring.social.account.dto.SocialUserDetail;
import com.spring.social.account.model.AdditionalDetail;
import com.spring.social.account.model.FBDeviceInfo;
import com.spring.social.account.model.FBEducational;
import com.spring.social.account.model.FBHomeTown;
import com.spring.social.account.model.FBInspiratinal;
import com.spring.social.account.model.FBMutualFriends;
import com.spring.social.account.model.FBMutualLikes;
import com.spring.social.account.model.FBSports;
import com.spring.social.account.model.FBTaggedPlaces;
import com.spring.social.account.model.FBWorkEntry;
import com.spring.social.account.model.FacebookImage;
import com.spring.social.account.model.FbFavouriteAthlete;
import com.spring.social.account.model.FbFavouriteTeam;
import com.spring.social.account.model.UserDetail;

@Component
public class SocialAccountFactory {

	final static Logger logger = LoggerFactory.getLogger(SocialAccountFactory.class);

	@Autowired
	private GoogleConnectionFactory googleConnectionFactory;

	@Autowired
	private UserDetailDao userDetailDao;

	@Autowired
	WorkEntryDao workEntryDao;

	@Autowired
	TaggedPlaceDao taggedPlaceDao;

	@Autowired
	FBEducationalDao fBEducationalDao;

	@Autowired
	FBSportsDao fBSportsDao;

	@Autowired
	FBMutualLikesDao fBMutualLikesDao;

	@Autowired
	FbFavouriteAthleteDao fbFavouriteAthleteDao;

	@Autowired
	FBInspiratinalDao fBInspiratinalDao;

	@Autowired
	FbFavouriteTeamDao fbFavouriteTeamDao;

	@Autowired
	FBHomeTownDao fBHomeTownDao;

	@Autowired
	AdditionalDetailDao additionalDetailDao;

	@Autowired
	FBDeviceInfoDao fBDeviceInfoDao;

	@Autowired
	FBMutualFriendsDao fBMutualFriendsDao;

	@Value("${google.redirect.uri}")
	private String redirectionURI;

	public UserDetail getDetailsFromFacebook(SocialUserDetail socialUserDetail) {
		UserDetail userDetail = null;
		try {
			String accessToken = socialUserDetail.getAccessToken();
			FacebookTemplate facebookTemplate = new FacebookTemplate(accessToken);
			User userProfile = null;
			if (facebookTemplate != null) {
				userProfile = facebookTemplate.userOperations().getUserProfile();
				if (userProfile != null) {
					String profileImage = fetchProfileImage(userProfile);
					String emailId = userProfile.getEmail();
					logger.info("Email Id : " + emailId);
					// When Email Id is found in User Profile.
					UserDetail existingAccount = userDetailDao.findOne(socialUserDetail.getImsId());
					if (existingAccount == null) {
						userDetail = new UserDetail(userProfile, accessToken);
						userDetail.setImsId(socialUserDetail.getImsId());
						userDetail.setCreateDate(UserAPIConstants.SQL_TIMESTAMP_FORMAT.format(new Date()));
						userDetail.setProfileImage(profileImage != null ? profileImage : null);
						userDetailDao.save(userDetail);
					} else {
						userDetail = existingAccount;
						userDetailDao.save(existingAccount);
					}
					getAllDetailsFromFacebook(facebookTemplate, userProfile.getId(), socialUserDetail.getAccessToken());
					getAddtionalDetailsFromFacebook(userProfile, socialUserDetail.getImsId(),
							socialUserDetail.getAccessToken());
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
		return userDetail;
	}

	public UserDetail getDetailsFromGoogle(SocialUserDetail socialUserDetail) {
		UserDetail userDetail = null;
		try {
			String accessToken = socialUserDetail.getAccessToken();
			Google google = googleConnectionFactory.createConnection(
					googleConnectionFactory.getOAuthOperations().exchangeForAccess(accessToken, redirectionURI, null))
					.getApi();
			accessToken = google.getAccessToken();

			// After access token we will be fetching details.
			Person person = null;
			GoogleTemplate plusTemplate = new GoogleTemplate(accessToken);
			person = plusTemplate.plusOperations().getGoogleProfile();
			if (person != null) {
				if (person.getAccountEmail() != null) {
					UserDetail existingAccount = userDetailDao.findAccountByEmailId(person.getAccountEmail());
					if (existingAccount == null) {
						userDetail = new UserDetail(person);
						userDetail.setPassword(accessToken);
						userDetail.setImsId(socialUserDetail.getImsId());
						userDetail.setCreateDate(UserAPIConstants.SQL_TIMESTAMP_FORMAT.format(new Date()));
						userDetailDao.save(userDetail);
					} else {
						existingAccount.setPassword(accessToken);
						userDetailDao.save(existingAccount);
						userDetail = existingAccount;
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
		return userDetail;
	}

	public void getAllDetailsFromFacebook(FacebookTemplate facebookTemplate, String userFacebookId,
			String access_token) {
		try {
			logger.info("#####" + new ObjectMapper().writeValueAsString(facebookTemplate.userOperations()));
			System.out.println("#####" + new ObjectMapper().writeValueAsString(facebookTemplate.userOperations()));
			getEducationalFromFacebook(facebookTemplate, userFacebookId);
			getFavouriteAthletesFromFacebook(facebookTemplate, userFacebookId);
			getTaggedPlacesFromFacebook(facebookTemplate, userFacebookId);
			getWorkEntryFromFacebook(facebookTemplate, userFacebookId);
			getSportsFromFacebook(facebookTemplate, userFacebookId);
			getInspirationalFromFacebook(facebookTemplate, userFacebookId);
			getHomeTownFromFacebook(facebookTemplate, userFacebookId);
			getFavouriteTeamsFromFacebook(facebookTemplate, userFacebookId);
			geteviceFromFacebook(facebookTemplate, userFacebookId);
			getMutualFriends(facebookTemplate.userOperations().getUserProfile());
			getMutualLikes(facebookTemplate.userOperations().getUserProfile(), access_token);
			// getInterestedInFromFacebook(facebookTemplate);
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public void getMutualFriends(User userProfile) {
		try {
			List<FBMutualFriends> fbMutualFriends = new ArrayList<FBMutualFriends>();
			Object obj = userProfile.getExtraData().get("context");
			if (obj != null) {
				Map<String, Object> dataMap = (Map<String, Object>) ((LinkedHashMap<String, Object>) (obj))
						.get("mutual_friends");
				if (dataMap.get("data") != null) {
					List<LinkedHashMap<String, String>> objList = (ArrayList<LinkedHashMap<String, String>>) dataMap
							.get("data");
					/*
					 * LinkedHashMap<String, String> moreDetails =
					 * (LinkedHashMap<String, String>) dataMap.get("paging");
					 * LinkedHashMap<String, String> summary =
					 * (LinkedHashMap<String, String>) dataMap.get("summary");
					 */
					Iterator<LinkedHashMap<String, String>> iterator = objList.iterator();
					while (iterator.hasNext()) {
						LinkedHashMap<String, String> dataObj = iterator.next();
						FBMutualFriends fBMutualLikes = new FBMutualFriends();
						fBMutualLikes.setFriendFacebookId(dataObj.get("id"));
						fBMutualLikes.setFriendName(dataObj.get("name"));
						fBMutualLikes.setUserFaceBookId(userProfile.getId());
						fbMutualFriends.add(fBMutualLikes);
					}
				}
			}
			if (fbMutualFriends.size() > 0) {
				fBMutualFriendsDao.deleteByUserFaceBookId(userProfile.getId());
				fBMutualFriendsDao.save(fbMutualFriends);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	/**
	 * Method will fetch profile image
	 * 
	 * @param userProfile
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<LinkedHashMap<String, String>> fetchMutualLikes(String url) {
		try {
			URI uri = URIBuilder.fromUri(url).build();
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(uri.toString());
			HttpResponse response = httpClient.execute(get);
			InputStream inputStream = response.getEntity().getContent();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				Map<String, List<LinkedHashMap<String, String>>> map = new HashMap<String, List<LinkedHashMap<String, String>>>();
				map = new ObjectMapper().readValue(line, map.getClass());
				return map.get("data");
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void getMutualLikes(User userProfile, String access_token) {
		try {
			List<FBMutualLikes> mutualLikes = new ArrayList<FBMutualLikes>();
			Object obj = userProfile.getExtraData().get("context");
			if (obj != null) {
				Map<String, Object> dataMap = (Map<String, Object>) ((LinkedHashMap<String, Object>) (obj))
						.get("mutual_likes");
				if (dataMap.get("data") != null) {
					List<LinkedHashMap<String, String>> objList = (ArrayList<LinkedHashMap<String, String>>) dataMap
							.get("data");
					LinkedHashMap<String, String> moreDetails = (LinkedHashMap<String, String>) dataMap.get("paging");
//					LinkedHashMap<String, String> summary = (LinkedHashMap<String, String>) dataMap.get("summary");
					Iterator<LinkedHashMap<String, String>> iterator = objList.iterator();
					while (iterator.hasNext()) {
						LinkedHashMap<String, String> dataObj = iterator.next();
						FBMutualLikes fBMutualLikes = new FBMutualLikes();
						fBMutualLikes.setLikeId(dataObj.get("id"));
						fBMutualLikes.setLikeName(dataObj.get("name"));
						fBMutualLikes.setUserFaceBookId(userProfile.getId());
						mutualLikes.add(fBMutualLikes);
					}
					if (mutualLikes.size() > 0) {
						fBMutualLikesDao.deleteByUserFaceBookId(userProfile.getId());
						fBMutualLikesDao.save(mutualLikes);
					}

					mutualLikes.clear();
					if (moreDetails != null && moreDetails.get("next") != null
							&& moreDetails.get("next").indexOf("limit") != -1) {
						String next = moreDetails.get("next");
						int index = next.indexOf("?");
						next = next.substring(0, index);
						next += "?limit=200&access_token=" + access_token;
						List<LinkedHashMap<String, String>> list = fetchMutualLikes(next);
						if (list != null && list.size() > 0) {
							Iterator<LinkedHashMap<String, String>> newIterator = list.iterator();
							while (newIterator.hasNext()) {
								LinkedHashMap<String, String> dataObj = newIterator.next();
								FBMutualLikes fBMutualLikes = new FBMutualLikes();
								fBMutualLikes.setLikeId(dataObj.get("id"));
								fBMutualLikes.setLikeName(dataObj.get("name"));
								fBMutualLikes.setUserFaceBookId(userProfile.getId());
								mutualLikes.add(fBMutualLikes);
							}
						}

					}
				}
			}
			if (mutualLikes.size() > 0) {
				fBMutualLikesDao.deleteByUserFaceBookId(userProfile.getId());
				fBMutualLikesDao.save(mutualLikes);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	/**
	 * Method will fetch additional information
	 * 
	 * @param userProfile
	 * @param imsId
	 */
	public void getAddtionalDetailsFromFacebook(User userProfile, String imsId, String accessToken) {
		try {
			if (userProfile != null && (userProfile.getRelationshipStatus() != null || userProfile.getAgeRange() != null
					|| userProfile.getLocation() != null)) {
				AdditionalDetail additionalDetail = new AdditionalDetail();
				additionalDetail.setAgeRange(userProfile.getAgeRange().name());
				additionalDetail.setRelationShipStatus(userProfile.getRelationshipStatus());
				additionalDetail.setUserFaceBookId(userProfile.getId());
				additionalDetail.setAbout(userProfile.getAbout());
				additionalDetail.setImsId(imsId);
				if (userProfile.getLocation() != null) {
					additionalDetail.setCurrentLocationId(userProfile.getLocation().getId());
					additionalDetail.setCurrentLocation(userProfile.getLocation().getName());
				}
				additionalDetail.setAccessToken(accessToken);
				additionalDetailDao.deleteByUserFaceBookId(userProfile.getId());
				additionalDetailDao.save(additionalDetail);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void geteviceFromFacebook(FacebookTemplate facebookTemplate, String userFaceBookId) {
		List<FBDeviceInfo> deviceList = new ArrayList<FBDeviceInfo>();
		try {
			List<Device> devices = facebookTemplate.userOperations().getUserProfile().getDevices();
			if (devices != null && devices.size() > 0) {
				Iterator<Device> iterator = devices.iterator();
				while (iterator.hasNext()) {
					Device device = iterator.next();
					if (device.getOS() != null) {
						FBDeviceInfo fBDeviceInfo = new FBDeviceInfo();
						fBDeviceInfo.setDeviceOs(device.getOS());
						fBDeviceInfo.setUserFaceBookId(userFaceBookId);
						deviceList.add(fBDeviceInfo);
					}
				}
			}
			if (deviceList != null && deviceList.size() > 0) {
				fBDeviceInfoDao.deleteByUserFaceBookId(userFaceBookId);
				fBDeviceInfoDao.save(deviceList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getHomeTownFromFacebook(FacebookTemplate facebookTemplate, String userFaceBookId) {
		FBHomeTown fBHomeTown = null;
		try {
			Reference reference = facebookTemplate.userOperations().getUserProfile().getHometown();
			if (reference != null) {
				fBHomeTown = new FBHomeTown();
				fBHomeTown.setHomeTownId(reference.getId());
				fBHomeTown.setHomeTownName(reference.getName());
				fBHomeTown.setUserFaceBookId(userFaceBookId);
				fBHomeTownDao.deleteByUserFaceBookId(userFaceBookId);
				fBHomeTownDao.save(fBHomeTown);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
		logger.info("fBHomeTown :>>" + fBHomeTown);
	}

	public void getInspirationalFromFacebook(FacebookTemplate facebookTemplate, String userFaceBookId) {
		List<FBInspiratinal> inspirationalList = new ArrayList<FBInspiratinal>();
		try {
			List<Reference> referenceList = facebookTemplate.userOperations().getUserProfile().getInspirationalPeople();
			if (referenceList != null) {
				Iterator<Reference> iterator = referenceList.iterator();
				while (iterator.hasNext()) {
					Reference reference = iterator.next();
					if (reference.getName() != null) {
						FBInspiratinal fBInspiratinal = new FBInspiratinal();
						fBInspiratinal.setInspirationalId(reference.getId());
						fBInspiratinal.setInspirationalName(reference.getName());
						fBInspiratinal.setUserFaceBookId(userFaceBookId);
						inspirationalList.add(fBInspiratinal);
					}
				}
			}
			if (inspirationalList.size() > 0) {
				fBInspiratinalDao.deleteByUserFaceBookId(userFaceBookId);
				fBInspiratinalDao.save(inspirationalList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getWorkEntryFromFacebook(FacebookTemplate facebookTemplate, String userFaceBookId) {
		List<FBWorkEntry> workList = new ArrayList<FBWorkEntry>();
		try {
			List<WorkEntry> workEntries = facebookTemplate.userOperations().getUserProfile().getWork();
			if (workEntries != null) {
				Iterator<WorkEntry> iterator = workEntries.iterator();
				while (iterator.hasNext()) {
					WorkEntry workEntry = iterator.next();
					if (workEntry.getEmployer() != null || workEntry.getLocation() != null
							|| workEntry.getPosition() != null) {
						FBWorkEntry fbWorkEntry = new FBWorkEntry();
						fbWorkEntry.setUserFaceBookId(userFaceBookId);
						if (workEntry.getEmployer() != null) {
							fbWorkEntry.setEmployeerId(workEntry.getEmployer().getId());
							fbWorkEntry.setEmployeeName(workEntry.getEmployer().getName());
						}
						if (workEntry.getLocation() != null) {
							fbWorkEntry.setLocationId(workEntry.getLocation().getId());
							fbWorkEntry.setLocationName(workEntry.getLocation().getName());
						}
						if (workEntry.getPosition() != null) {
							fbWorkEntry.setPositionId(workEntry.getPosition().getId());
							fbWorkEntry.setPositionName(workEntry.getPosition().getName());
						}
						fbWorkEntry.setStartDate(workEntry.getStartDate());
						fbWorkEntry.setEndDate(workEntry.getEndDate());
						workList.add(fbWorkEntry);
					}
				}
			}
			if (workList != null && workList.size() > 0) {
				workEntryDao.deleteByUserFaceBookId(userFaceBookId);
				workEntryDao.save(workList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
		logger.info("workList :>> " + workList);
	}

	public void getInterestedInFromFacebook(FacebookTemplate facebookTemplate) {
		try {
			List<String> interests = facebookTemplate.userOperations().getUserProfile().getInterestedIn();
			if (interests != null) {
				Iterator<String> iterator = interests.iterator();
				while (iterator.hasNext()) {
					String interest = iterator.next();
					System.out.println("######getInterestedInFromFacebook" + interest);
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getTaggedPlacesFromFacebook(FacebookTemplate facebookTemplate, String userFacebookId) {
		List<FBTaggedPlaces> taggedPlaceList = new ArrayList<FBTaggedPlaces>();
		try {
			List<PlaceTag> placeTagList = facebookTemplate.userOperations().getTaggedPlaces();
			if (placeTagList != null) {
				Iterator<PlaceTag> iterator = placeTagList.iterator();
				while (iterator.hasNext()) {
					PlaceTag placeTag = iterator.next();
					if (placeTag.getPlace() != null && placeTag.getPlace().getId() != null) {
						FBTaggedPlaces fBTaggedPlaces = new FBTaggedPlaces();
						fBTaggedPlaces.setTaggedId(placeTag.getId());
						fBTaggedPlaces.setTaggedPlaceId(placeTag.getPlace().getId());
						fBTaggedPlaces.setTaggedPLaceName(placeTag.getPlace().getName());
						fBTaggedPlaces.setUserFaceBookId(userFacebookId);
						if (placeTag.getPlace().getLocation() != null) {
							fBTaggedPlaces.setLocationId(placeTag.getPlace().getLocation().getId());
							fBTaggedPlaces.setLocationName(placeTag.getPlace().getLocation().getName());
							fBTaggedPlaces.setLongitude(placeTag.getPlace().getLocation().getLongitude() + "");
							fBTaggedPlaces.setLatitude(placeTag.getPlace().getLocation().getLatitude() + "");
							fBTaggedPlaces.setCountry(placeTag.getPlace().getLocation().getCountry());
							fBTaggedPlaces.setStreet(placeTag.getPlace().getLocation().getStreet());
							fBTaggedPlaces.setZip(placeTag.getPlace().getLocation().getZip());
						}
						taggedPlaceList.add(fBTaggedPlaces);
					}
				}
				if (placeTagList.size() > 0) {
					taggedPlaceDao.deleteByUserFaceBookId(userFacebookId);
					taggedPlaceDao.save(taggedPlaceList);
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getEducationalFromFacebook(FacebookTemplate facebookTemplate, String userFacebookId) {
		List<FBEducational> educationalList = new ArrayList<FBEducational>();
		try {
			List<EducationExperience> educationExperienceList = facebookTemplate.userOperations().getUserProfile()
					.getEducation();
			if (educationExperienceList != null) {
				Iterator<EducationExperience> iterator = educationExperienceList.iterator();
				while (iterator.hasNext()) {
					EducationExperience educationExperience = iterator.next();
					if (educationExperience.getSchool() != null || educationExperience.getConcentration() != null) {
						FBEducational fbEducational = new FBEducational();
						fbEducational.setUserFaceBookId(userFacebookId);
						fbEducational.setSchoolId(educationExperience.getSchool().getId());
						fbEducational.setSchoolName(educationExperience.getSchool().getName());
						if (educationExperience.getDegree() != null) {
							fbEducational.setDegreeId(educationExperience.getDegree().getId());
							fbEducational.setDegreeName(educationExperience.getDegree().getName());
						}
						if (educationExperience.getYear() != null) {
							fbEducational.setYearId(educationExperience.getYear().getId());
							fbEducational.setYearName(educationExperience.getYear().getName());
						}
						if (educationExperience.getConcentration() != null
								&& educationExperience.getConcentration().size() > 0) {
							fbEducational.setConectrationId(educationExperience.getConcentration().get(0).getId());
							fbEducational.setConcentrationName(educationExperience.getConcentration().get(0).getName());
						}
						educationalList.add(fbEducational);
					}
				}
			}
			if (educationalList.size() > 0) {
				fBEducationalDao.deleteByUserFaceBookId(userFacebookId);
				fBEducationalDao.save(educationalList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getSportsFromFacebook(FacebookTemplate facebookTemplate, String userFacebookId) {
		List<FBSports> fBSportsList = new ArrayList<FBSports>();
		try {
			List<Experience> experienceList = facebookTemplate.userOperations().getUserProfile().getSports();
			if (experienceList != null) {
				Iterator<Experience> iterator = experienceList.iterator();
				while (iterator.hasNext()) {
					Experience experience = iterator.next();
					if (experience.getName() != null) {
						FBSports fBSports = new FBSports();
						fBSports.setSportsId(experience.getId());
						fBSports.setSportsName(experience.getName());
						fBSports.setUserFaceBookId(userFacebookId);
						fBSportsList.add(fBSports);
					}
				}
			}
			if (fBSportsList.size() > 0) {
				fBSportsDao.deleteByUserFaceBookId(userFacebookId);
				fBSportsDao.save(fBSportsList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getFavouriteAthletesFromFacebook(FacebookTemplate facebookTemplate, String userFacebookId) {
		List<FbFavouriteAthlete> fbFavouriteAthleteList = new ArrayList<FbFavouriteAthlete>();
		try {
			List<Reference> referenceList = facebookTemplate.userOperations().getUserProfile().getFavoriteAtheletes();
			if (referenceList != null) {
				Iterator<Reference> iterator = referenceList.iterator();
				while (iterator.hasNext()) {
					Reference reference = iterator.next();
					if (reference.getName() != null) {
						FbFavouriteAthlete fbFavouriteAthlete = new FbFavouriteAthlete();
						fbFavouriteAthlete.setAthleteId(reference.getId());
						fbFavouriteAthlete.setAthleteName(reference.getName());
						fbFavouriteAthlete.setUserFaceBookId(userFacebookId);
						fbFavouriteAthleteList.add(fbFavouriteAthlete);
					}
				}
			}
			if (fbFavouriteAthleteList.size() > 0) {
				fbFavouriteAthleteDao.deleteByUserFaceBookId(userFacebookId);
				fbFavouriteAthleteDao.save(fbFavouriteAthleteList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	public void getFavouriteTeamsFromFacebook(FacebookTemplate facebookTemplate, String userFacebookId) {
		List<FbFavouriteTeam> fbFavouriteTeamList = new ArrayList<FbFavouriteTeam>();
		try {
			List<Reference> referenceList = facebookTemplate.userOperations().getUserProfile().getFavoriteTeams();
			if (referenceList != null) {
				Iterator<Reference> iterator = referenceList.iterator();
				while (iterator.hasNext()) {
					Reference reference = iterator.next();
					if (reference.getName() != null) {
						FbFavouriteTeam fbFavouriteTeam = new FbFavouriteTeam();
						fbFavouriteTeam.setTeamId(reference.getId());
						fbFavouriteTeam.setTeamName(reference.getName());
						fbFavouriteTeam.setUserFaceBookId(userFacebookId);
						fbFavouriteTeamList.add(fbFavouriteTeam);
					}
				}
			}
			if (fbFavouriteTeamList.size() > 0) {
				fbFavouriteTeamDao.deleteByUserFaceBookId(userFacebookId);
				fbFavouriteTeamDao.save(fbFavouriteTeamList);
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
	}

	/**
	 * Method will fetch profile image
	 * 
	 * @param userProfile
	 * @return
	 */
	private String fetchProfileImage(User userProfile) {
		try {
			URI uri = URIBuilder.fromUri(GraphApi.GRAPH_API_URL + userProfile.getId() + "/picture" + "?type="
					+ ImageType.SMALL.toString().toLowerCase() + "&redirect=false").build();
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(uri.toString());
			HttpResponse response = httpClient.execute(get);
			InputStream inputStream = response.getEntity().getContent();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				FacebookImage facebookImage = objectMapper.readValue(line, FacebookImage.class);
				logger.info("Image Details : " + facebookImage);
				return facebookImage.getData().getUrl();
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " + exception.getMessage());
		}
		return null;
	}

	/**
	 * Code is added to remove video upload limits fields as that was out of
	 * integer range not supported in Spring facebook social
	 */
	@PostConstruct
	private void init() {
		// hack for the login of facebook.
		try {
			String[] fieldsToMap = { "id", "about", "age_range", "bio", "birthday", "context", "cover", "currency",
					"devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender",
					"hometown", "inspirational_people", "installed", "install_type", "is_verified", "languages",
					"last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format",
					"political", "quotes", "payment_pricepoints", "relationship_status", "religion",
					"security_settings", "significant_other", "sports", "test_group", "timezone", "third_party_id",
					"updated_time", "verified", "viewer_can_send_gift", "website", "work" };

			Field field = Class.forName("org.springframework.social.facebook.api.UserOperations")
					.getDeclaredField("PROFILE_FIELDS");
			field.setAccessible(true);

			Field modifiers = field.getClass().getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(null, fieldsToMap);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
