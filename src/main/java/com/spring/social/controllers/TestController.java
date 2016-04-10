package com.spring.social.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.spring.social.account.constants.UserAPIConstants;
import com.spring.social.account.dto.SocialUserDetail;
import com.spring.social.account.model.MyAccessToken;
import com.spring.social.security.SocialAccountFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

@RestController
public class TestController {

	final static Logger logger = LoggerFactory.getLogger(TestController.class);

	@Autowired
	SocialAccountFactory socialAccountFactory;

	@Value("${facebook.appID}")
	private String fbAppId;

	@Value("${facebook.appSecret}")
	private String fbAppSecret;

	@Value("${facebook.login.code.url}")
	private String fbLoginURI;

	@Value("${facebook.login.redirect.url}")
	private String fbRedirectURi;

	@Value("${facebook.login.code.cancel_url}")
	private String fbCancelURi;

	@Value("${google.appID}")
	private String googleAppId;

	@Value("${google.appSecret}")
	private String googleAppSecret;

	@Value("${google.login.code.url}")
	private String googleLoginURI;

	@Value("${google.redirect.uri}")
	private String googleRedirectURi;

	@Value("${twitter.login.consumerapi}")
	private String consumerAPI;

	@Value("${twitter.login.consumersecret}")
	private String consumerKey;

	@Value("${twitter.redirect.uri}")
	private String twitterRedirectURi;

	@Autowired
	MyAccessToken myAccessToken;

	@RequestMapping(
		value = "/socialLinks/",
		method = { RequestMethod.GET },
		produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> fetchSocialLinks() throws TwitterException {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			Twitter twitter = new TwitterFactory().getInstance();
			// Twitter
			twitter.setOAuthConsumer(consumerAPI, consumerKey);
			RequestToken requestToken = twitter.getOAuthRequestToken(twitterRedirectURi);
			String token = requestToken.getToken();
			String tokenSecret = requestToken.getTokenSecret();
			String authUrl = requestToken.getAuthorizationURL();
			myAccessToken = new MyAccessToken(token, tokenSecret);
			responseMap.put("twitterLogin", authUrl);
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error("Exception occured {}", exception);
		}

		String fbLogin = MessageFormat.format(fbLoginURI, new Object[] { fbAppId, fbRedirectURi, fbCancelURi });
		responseMap.put("fbLogin", fbLogin);

		String googleLogin = MessageFormat.format(googleLoginURI, new Object[] { googleAppId, googleRedirectURi });
		responseMap.put("googleLogin", googleLogin);

		return responseMap;
	}

	private String getFBGraphUrl(String code) {
		String fbGraphUrl = "";
		try {
			fbGraphUrl = "https://graph.facebook.com/oauth/access_token?" + "client_id=" + fbAppId + "&redirect_uri="
					+ URLEncoder.encode(fbRedirectURi, "UTF-8") + "&client_secret=" + fbAppSecret + "&code=" + code;
		} catch (Exception e) {
			logger.error("Exception occured while forming facebook login url", e);
		}
		return fbGraphUrl;
	}

	@RequestMapping(
		value = "/socialLogin",
		method = RequestMethod.GET)
	public void socialLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		boolean isSuccess = false;
		try {
			String socialMedia = request.getParameter("media");
			String accessToken = null;
			String code = request.getParameter("code");
			if (code != null || UserAPIConstants.TWITTER.equalsIgnoreCase(socialMedia)) {
				switch (socialMedia) {
				case UserAPIConstants.FACEBOOK:
					accessToken = getAccessToken(request.getParameter("code"), socialMedia);
					if (accessToken != null) {
						isSuccess = true;
						socialAccountFactory
								.getDetailsFromFacebook(new SocialUserDetail(accessToken, UUID.randomUUID().toString(), UserAPIConstants.FACEBOOK));
					}
					break;
				case UserAPIConstants.GOOGLE:
					accessToken = request.getParameter("code");
					isSuccess = true;
					socialAccountFactory
							.getDetailsFromGoogle(new SocialUserDetail(accessToken, UUID.randomUUID().toString(), UserAPIConstants.GOOGLE));
					break;
				case UserAPIConstants.TWITTER:
					Twitter twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(consumerAPI, consumerKey);
					String verifier = request.getParameter("oauth_verifier");
					RequestToken requestToken = new RequestToken(myAccessToken.getToken(), myAccessToken.getTokensecret());
					AccessToken twitterAccessToken = twitter.getOAuthAccessToken(requestToken, verifier);
					twitter.setOAuthAccessToken(twitterAccessToken);
					twitter4j.User user = twitter.verifyCredentials();
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while forming facebook login url", e);
		}
		if (isSuccess) {
			response.sendRedirect("success.html");
		} else {
			response.sendRedirect("failure.html");
		}
	}

	/**
	 * 
	 * @param parameter
	 * @return
	 */
	private String getAccessToken(String code, String media) {
		StringBuilder accessToken = new StringBuilder();
		if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(media)) {
			URL fbGraphURL;
			try {
				fbGraphURL = new URL(getFBGraphUrl(code));
			} catch (MalformedURLException e) {
				logger.error("Exception occured while forming url", e);
				throw new RuntimeException("Invalid code received " + e);
			}
			URLConnection fbConnection;
			BufferedReader fbBufferdReader = null;
			try {
				fbConnection = fbGraphURL.openConnection();
				fbBufferdReader = new BufferedReader(new InputStreamReader(fbConnection.getInputStream()));
				String inputLine;
				while ((inputLine = fbBufferdReader.readLine()) != null) {
					accessToken.append(inputLine + "\n");
				}
			} catch (IOException e) {
				logger.error("Exception occured while fetching access token");
				throw new RuntimeException("Unable to connect with Facebook " + e);
			} finally {
				IOUtils.closeQuietly(fbBufferdReader);
			}
			if (accessToken.toString().startsWith("{")) {
				logger.error("ERROR: Access Token Invalid:");
				throw new RuntimeException("ERROR: Access Token Invalid: " + accessToken);
			}
			return accessToken.toString().substring(accessToken.toString().indexOf("=") + 1, accessToken.toString().indexOf("&expires="));
		}
		return null;
	}

}
