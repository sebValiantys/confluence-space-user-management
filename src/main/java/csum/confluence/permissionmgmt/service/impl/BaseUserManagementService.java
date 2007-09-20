/**
 * Copyright (c) 2007, Custom Space User Management Plugin Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space User Management Plugin Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.query.EmailTermQuery;
import com.atlassian.user.search.query.FullNameTermQuery;
import com.atlassian.user.search.query.TermQuery;
import com.atlassian.user.search.query.UserNameTermQuery;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.UserManagementService;
import csum.confluence.permissionmgmt.service.exception.FindException;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import com.dolby.confluence.net.ldap.LDAPException;
import csum.confluence.permissionmgmt.util.ldap.LDAPHelper;
import com.dolby.confluence.net.ldap.LDAPUser;
import csum.confluence.permissionmgmt.util.paging.LazyLoadingUserByUsernamePager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public abstract class BaseUserManagementService implements UserManagementService {

    protected UserAccessor userAccessor;
    private CustomPermissionConfiguration customPermissionConfiguration;
    protected Log log = LogFactory.getLog(this.getClass());

    public BaseUserManagementService() {
        log.debug("BaseUserManagementService start constructor");
        userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("BaseUserManagementService end constructor");
    }

    protected LDAPUser getLDAPUser(String userid) throws ParserConfigurationException, LDAPException, IOException, SAXException {
        return LDAPHelper.getLDAPUser(getCustomPermissionConfiguration(), userid);
    }

    public AdvancedUserQueryResults findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {
        log.debug("findUsers() called.");
        AdvancedUserQueryResults results = new AdvancedUserQueryResults();

        //TODO: this is really slow with osuser search. must use http://confluence.atlassian.com/display/DOC/How+to+Improve+User+Search+Performance

        Pager pager = new DefaultPager(new ArrayList());
        if (advancedUserQuery.isUsernameSearchDefined()) {
            try {
                UserNameTermQuery query = new UserNameTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                //results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                LogUtil.warnWithRemoteUserInfo(log, "query by username failed due to EntityException", e);
                results.setMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                LogUtil.warnWithRemoteUserInfo(log, "Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'", e);
                results.setMessage("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'");
            }
        }

        if (advancedUserQuery.isFullnameSearchDefined()) {
            try {
                FullNameTermQuery query = new FullNameTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                //results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                LogUtil.warnWithRemoteUserInfo(log, "query by user fullname failed due to EntityException", e);
                results.setMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                LogUtil.warnWithRemoteUserInfo(log, "Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'", e);
                results.setMessage("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'");
            }
        }

        if (advancedUserQuery.isEmailSearchDefined()) {
            try {
                EmailTermQuery query = new EmailTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                //results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                LogUtil.warnWithRemoteUserInfo(log, "query by user email failed due to EntityException", e);
                results.setMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                LogUtil.warnWithRemoteUserInfo(log, "Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'", e);
                results.setMessage("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'");
            }
        }

        results.setUsers(pager);

        return results;
    }

    public Pager findUsersForGroup(String groupName, ServiceContext context) throws FindException {
        log.debug("findUsersForGroup(groupName) called. groupName='" + groupName + "'");
        Group group = userAccessor.getGroup(groupName);
        if (group == null) {
            throw new FindException("Group '" + groupName + "' not found");
        }
        return findUsersForGroup(group);
    }

    private Pager findUsersForGroup(Group group) throws FindException {
        log.debug("findUsersForGroup(Group) called.");
        if (group == null) {
            throw new FindException("Group was null");
        }
        Pager usernamePager = userAccessor.getMemberNames(group);
        if (usernamePager == null) {
            throw new FindException("Did not find users for group '" + group.getName() + "'");
        }
        LazyLoadingUserByUsernamePager userPager = new LazyLoadingUserByUsernamePager();
        userPager.setUsernamePager(usernamePager);
        userPager.setUserAccessor(this.userAccessor);
        return userPager;
    }

    public Pager findUsersWhoseNameStartsWith(String partialName, ServiceContext context) {
        log.debug("findUsersWhoseNameStartsWith() called. partialName='" + partialName + "'");
        Pager pager = null;

        try {
            UserNameTermQuery query = new UserNameTermQuery(partialName, TermQuery.SUBSTRING_STARTS_WITH);
            SearchResult searchResult = userAccessor.findUsers(query);
            pager = searchResult.pager();
        }
        catch (EntityException e) {
            LogUtil.errorWithRemoteUserInfo(log, "Error finding username that starts with " + partialName, e);
        }

        return pager;
    }

    protected String getAddUsersByUsernameToGroupsErrorMessage(List usersNotFound, List groupsNotFound, Map userIdToGroupNameMapForMembershipAdditionProblems, ServiceContext context) {
        String msg = "";
        String concat = "";
        if (usersNotFound.size() > 0) {
            msg += context.getText("manager.error.usersnotfound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(usersNotFound) + ".";
            concat = " ";
        }

        if (groupsNotFound.size() > 0) {
            msg += concat;
            msg += context.getText("manager.error.groupsdidnotexist") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(groupsNotFound) + ".";
            concat = " ";
        }

        if (userIdToGroupNameMapForMembershipAdditionProblems.size() > 0) {
            Iterator iter = userIdToGroupNameMapForMembershipAdditionProblems.keySet().iterator();
            while(iter.hasNext()) {
                String groupName = (String)iter.next();
                String userid = (String)userIdToGroupNameMapForMembershipAdditionProblems.get(groupName);
                msg += concat;
                msg += context.getText("manager.error.problemaddingusertogroup", new String[] {userid, groupName});
                concat = " ";
            }
        }

        return msg;
    }

    protected String getRemoveUsersByUsernameFromGroupsErrorMessage(List usersNotFound, List groupsNotFound, Map userIdToGroupNameMapForMembershipRemovalProblems, ServiceContext context) {
        String msg = "";
        String concat = "";
        if (usersNotFound.size() > 0) {
            msg += context.getText("manager.error.usersnotfound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(usersNotFound) + ".";
            concat = " ";
        }

        if (groupsNotFound.size() > 0) {
            msg += concat;
            msg += context.getText("manager.error.groupsdidnotexist") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(groupsNotFound) + ".";
        }

        if (userIdToGroupNameMapForMembershipRemovalProblems.size() > 0) {
            Iterator iter = userIdToGroupNameMapForMembershipRemovalProblems.keySet().iterator();
            while(iter.hasNext()) {
                String groupName = (String)iter.next();
                String userid = (String)userIdToGroupNameMapForMembershipRemovalProblems.get(groupName);
                msg += concat;
                msg += context.getText("manager.error.problemremovinguserfromgroup", new String[] {userid, groupName});
                concat = " ";
            }
        }

        return msg;
    }

    public boolean isMemberOf(String userName, String groupName) {
        log.debug("isMemberOf() called. userName=" + userName + " groupName=" + groupName);
        boolean result = false;
        Group group = userAccessor.getGroup(groupName);
        if (group != null) {
            Pager pager = userAccessor.getMemberNames(group);
            List memberNames = PagerUtils.toList(pager);
            if (memberNames != null) {
                result = memberNames.contains(userName);
            }
        }
        return result;
    }

    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

}
