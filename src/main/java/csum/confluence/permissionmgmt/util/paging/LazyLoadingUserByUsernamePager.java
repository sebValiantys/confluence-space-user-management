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

package csum.confluence.permissionmgmt.util.paging;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import csum.confluence.permissionmgmt.util.logging.LogUtil;

/**
 * @author Gary S. Weaver
 */
public class LazyLoadingUserByUsernamePager implements Pager {

    public final Log log = LogFactory.getLog(this.getClass());

    private Pager usernamePager;
    private UserAccessor userAccessor;

    public boolean isEmpty() {
        return getUsernamePager().isEmpty();
    }

    //TODO: Warning! This is not currently implemented to return User instead of username, so I think PagerUtils may not work...
    public Iterator iterator() {
        return getUsernamePager().iterator();
    }

    public List getCurrentPage() {
        List results = new ArrayList();
        Pager usernamePager = getUsernamePager();
        // must check for null here! (SUSR-54)
        if ( usernamePager != null ) {
            List usernames = usernamePager.getCurrentPage();
            if (usernames!=null) {
                for (int i=0; i<usernames.size(); i++) {
                    String username = (String)usernames.get(i);
                    User user = getUserAccessor().getUser(username);
                    results.add(user);
                }
            }
            else {
                log.debug("usernamePager.getCurrentPage() returned null. This may just be an empty group.");
            }
        }
        else {
            LogUtil.warnWithRemoteUserInfo(log, "usernamePager was null. Either was an empty group or an API issue. Look in debug logging for the groupname to look it up and be sure.");
        }
        return results;
    }

    public void nextPage() {
        getUsernamePager().nextPage();
    }

    public boolean onLastPage() {
        return getUsernamePager().onLastPage();
    }

    public void skipTo(int i) throws PagerException {
        getUsernamePager().skipTo(i);
    }

    public int getIndex() {
        return getUsernamePager().getIndex();
    }

    public int getIndexOfFirstItemInCurrentPage() {
        return getUsernamePager().getIndexOfFirstItemInCurrentPage();
    }

    public Pager getUsernamePager() {
        return usernamePager;
    }

    public void setUsernamePager(Pager usernamePager) {
        this.usernamePager = usernamePager;
    }

    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
