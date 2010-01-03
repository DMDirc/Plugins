/*
 *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.dmdirc.addons.parser_twitter.api;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author shane
 */
public class TwitterUser {
    /** What is the screen name of this user? */
    private final String screenName;

    /** What is the user id of this user? */
    private final long userID;
    
    /** What is the real name of this user? */
    private final String realName;

    /** Are we following the user? */
    private final boolean following;

    /** Is the user following us? */
    private boolean followingUs = false;

    /** What was the last status of this user? */
    private final TwitterStatus lastStatus;

    /** API Object that owns this. */
    private final TwitterAPI myAPI;

    /** URL to users profile picture. */
    private String myProfilePicture;

    /** URL from users profile. */
    private String myURL;

    /** Location of user */
    private String myLocation;

    /** Description of user */
    private String myDescription;

    /** Time user registered with twitter. */
    private Long myRegisteredTime;

    /**
     * Create a unknown TwitterUser
     *
     * @param api 
     * @param screenName Screen name for the user.
     */
    protected TwitterUser(final TwitterAPI api, final String screenName) {
        this(api, screenName, -1, "", false);
    }

    /**
     * Create a new TwitterUser
     *
     * @param api
     * @param screenName Screen name for the user.
     * @param userID User ID for the user.
     * @param realName Realname for the user.
     * @param following Are we following the user?
     */
    protected TwitterUser(final TwitterAPI api, final String screenName, final long userID, final String realName, final boolean following) {
        this.myAPI = api;
        this.screenName = screenName;
        this.userID = userID;
        this.realName = realName;
        this.following = following;
        this.lastStatus = null;
        this.myProfilePicture = "";
        this.myURL = "";
        this.myLocation = "";
        this.myDescription = "";
        this.myRegisteredTime = 0L;
    }

    /**
     * Create a twitter user from a node!
     *
     * @param api
     * @param node Node to use.
     */
    protected TwitterUser(final TwitterAPI api, final Node node) {
        this(api, node, null);
    }

    /**
     * Create a twitter user from a node, with a pre-defined status.
     *
     * @param api
     * @param node Node to use.
     * @param status Status to use
     */
    protected TwitterUser(final TwitterAPI api, final Node node, final TwitterStatus status) {
        if (!(node instanceof Element)) { throw new TwitterRuntimeException("Can only use Element type nodes for user creation."); }

        final Element element = (Element) node;
        this.myAPI = api;

        this.realName = TwitterAPI.getElementContents(element, "name", "");
        this.screenName = (api.autoAt() ? "@" : "") + TwitterAPI.getElementContents(element, "screen_name", "");
        
        this.myProfilePicture = TwitterAPI.getElementContents(element, "profile_image_url", "");
        this.myURL = TwitterAPI.getElementContents(element, "url", "");
        this.myLocation = TwitterAPI.getElementContents(element, "location", "");
        this.myDescription = TwitterAPI.getElementContents(element, "description", "");

        this.myRegisteredTime = TwitterAPI.timeStringToLong(TwitterAPI.getElementContents(element, "created_at", ""), 0);

        this.userID = TwitterAPI.parseLong(TwitterAPI.getElementContents(element, "id", ""), -1);
        this.following = TwitterAPI.parseBoolean(TwitterAPI.getElementContents(element, "following", ""));

        // Check to see if a cached user object for us exists that we can
        // take some information from.
        final TwitterUser oldUser = api.getCachedUser(this.screenName);

        // First, are they following us back do we know?
        if (oldUser != null) {
            this.followingUs = oldUser.isFollowingUs();
        }

        TwitterStatus newStatus = null;
        boolean useOldStatus = true;

        // Now set the status, using either the oldStatus, the given status or null!
        if (status == null) {
            final TwitterStatus proposedStatus;
            final NodeList nodes = element.getElementsByTagName("status");
            if (nodes != null && nodes.getLength() > 0) {
                proposedStatus = new TwitterStatus(api, nodes.item(0), this.getScreenName());
                if (oldUser == null || oldUser.getStatus() == null || oldUser.getStatus().getID() < proposedStatus.getID()) {
                    useOldStatus = false;
                    newStatus = proposedStatus;
                }
            } else if (oldUser == null || oldUser.getStatus() == null) {
                useOldStatus = false;
                newStatus = new TwitterStatus(api, "", -1, -1, this.getScreenName(), System.currentTimeMillis());
            }
        } else {
            // Keep the status from the old version of this user if it has a
            // higher ID, regardless of the fact we were given a specific
            // status to use.
            if (oldUser == null || oldUser.getStatus() == null || oldUser.getStatus().getRetweetId() < status.getRetweetId()) {
                useOldStatus = false;
                newStatus = status;
            }
        }

        if (useOldStatus && oldUser != null && oldUser.getStatus() != null) {
            newStatus = oldUser.getStatus();
        }

        this.lastStatus = newStatus;
    }


    /**
     * Get the screen name for this user.
     *
     * @return this users screen name.
     */
    public String getScreenName() {
        return screenName;
    }
    
    /**
     * Get the id for this user.
     * 
     * @return this users id.
     */
    public long getID() {
        return userID;
    }

    /**
     * Get the real name for this user.
     * 
     * @return this users real name.
     */
    public String getRealName() {
        return realName;
    }

    /**
     * Are we following this user?
     * 
     * @return True if we are following this user, else false.
     */
    public boolean isFollowing() {
        return following;
    }

    /**
     * Get the last known status of this user
     *
     * @return Last known status.
     */
    public TwitterStatus getStatus() {
        return lastStatus;
    }

    /**
     * Are we being followed by this user?
     *
     * @return True if we are being followed by this user, else false.
     */
    public boolean isFollowingUs() {
        return followingUs;
    }

    /**
     * Change if this user is following us.
     *
     * @param followingUs The new value for this setting.
     */
    public void setFollowingUs(final boolean followingUs) {
        this.followingUs = followingUs;
    }

    /**
     * URL for users profile pic.
     *
     * @return URL for users profile pic.
     */
    public String getProfilePicture() {
        return myProfilePicture;
    }

    /**
     * URL for user.
     *
     * @return URL for user.
     */
    public String getURL() {
        return myURL;
    }

    /**
     * Description for user.
     *
     * @return Description for user.
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Location for user.
     *
     * @return Location for user.
     */
    public String getLocation() {
        return myLocation;
    }

    /**
     * Time user registered at.
     *
     * @return Time user registered at.
     */
    public Long getRegisteredTime() {
        return myRegisteredTime;
    }


}
