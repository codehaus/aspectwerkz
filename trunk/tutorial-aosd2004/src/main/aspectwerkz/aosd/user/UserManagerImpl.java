/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.user;

import aspectwerkz.aosd.persistence.PersistenceManager;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;

import java.util.StringTokenizer;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class UserManagerImpl implements UserManager {

    public User retrieveUser(String userId) {
        try {
            PersistenceManager pm = JispPersistenceManager.getInstance();

            User user = (User)pm.retrieve(User.class, userId);
            if (user == null) {
                StringTokenizer st = new StringTokenizer(userId, ".");
                user = new User(st.nextToken(), st.nextToken());
                pm.store(user);
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
