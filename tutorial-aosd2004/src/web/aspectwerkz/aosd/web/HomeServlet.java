/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.web;

import aspectwerkz.aosd.context.UserContext;
import aspectwerkz.aosd.context.Context;
import aspectwerkz.aosd.context.ContextException;
import aspectwerkz.aosd.persistence.PersistenceManager;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;
import aspectwerkz.aosd.user.User;
import aspectwerkz.aosd.user.Registry;
import aspectwerkz.aosd.addressbook.AddressBook;
import aspectwerkz.aosd.addressbook.Contact;
import aspectwerkz.aosd.ServiceManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * Demo app
 *
 * Usefull url:
 *
 * http://localhost:7014/aosd/demo
 * http://localhost:7014/aosd/demo?action=LOGIN&username=alex&pwd=alex
 * http://localhost:7014/aosd/demo?action=LIST
 * http://localhost:7014/aosd/demo?action=ADD&fn=jonas&ln=boner&em=jonas_email
 * http://localhost:7014/aosd/demo?action=ADD&fn=billy&ln=boy&em=billy_email
 * http://localhost:7014/aosd/demo?action=REMOVE&ids=billy.boy&ids=jonas.boner
 *
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class HomeServlet extends HttpServlet {

    static {
        ServiceManager.startServices();
    }

    private final static String ACTION_LOGIN = "LOGIN";
    private final static String ACTION_ADD = "ADD";
    private final static String ACTION_LIST = "LIST";
    private final static String ACTION_REMOVE = "REMOVE";

    private final static String KEY_ACTION = "action";
    private final static String KEY_USERNAME = "username";
    private final static String KEY_PASSWORD = "pwd";
    private final static String KEY_FIRSTNAME = "fn";
    private final static String KEY_LASTNAME = "ln";
    private final static String KEY_EMAIL = "em";
    private final static String KEY_IDS = "ids";

    private final static String SESSION_USER = "user";

    private final static String VIEW_ADRESSBOOK = "abd";

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        String action = httpServletRequest.getParameter(KEY_ACTION);
        try  {
            System.out.println(httpServletRequest.getSession().getAttribute(SESSION_USER));

            if (action == null)
                viewLogin(httpServletRequest, httpServletResponse);
            else if (ACTION_LOGIN.equals(action)) {
                actionLogin(httpServletRequest, httpServletResponse);
            } else if (ACTION_ADD.equals(action)) {
                actionAdd(httpServletRequest, httpServletResponse);
            } else if (ACTION_LIST.equals(action)) {
                actionList(httpServletRequest, httpServletResponse);
            } else if (ACTION_REMOVE.equals(action)) {
                actionRemove(httpServletRequest, httpServletResponse);
            }
        } catch (Exception e) {
            //TODO error message
            e.printStackTrace();
            viewHome(httpServletRequest, httpServletResponse);
        }
    }

    private void viewLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        //TODO
        System.out.println("LOGIN, action=HOME");
    }

    private void viewHome(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        //TODO
        System.out.println("HOME, action=ADD|LIST");
    }

    private void viewList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        //TODO
        System.out.println("LIST, action=REMOVE");
        AddressBook adb = (AddressBook)httpServletRequest.getAttribute(VIEW_ADRESSBOOK);
        for (Iterator contacts = adb.getContacts().iterator(); contacts.hasNext();) {
            System.out.println((Contact)contacts.next());
        }
    }

    private void actionLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String user = httpServletRequest.getParameter(KEY_USERNAME);
        String pwd = httpServletRequest.getParameter(KEY_PASSWORD);

        // feed user context info in session
        UserContext ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, user);
        ctx.put(Context.CREDENTIAL, pwd);
        httpServletRequest.getSession().setAttribute(SESSION_USER, ctx);
        System.out.println("  session created " + httpServletRequest.getSession());

        // Authentication thru AOP

        viewHome(httpServletRequest, httpServletResponse);
    }

    private String getUserKey(HttpSession session) {
        try  {
            UserContext ctx = (UserContext)session.getAttribute(SESSION_USER);
            return (String)ctx.get(Context.PRINCIPAL)+"."+(String)ctx.get(Context.CREDENTIAL);
        } catch (ContextException ce) {
            return null;
        }
    }

    private String getUsername(HttpSession session) {
        try  {
            UserContext ctx = (UserContext)session.getAttribute(SESSION_USER);
            return (String)ctx.get(Context.PRINCIPAL);
        } catch (ContextException ce) {
            return null;
        }
    }

    private String getPassword(HttpSession session) {
        try  {
            UserContext ctx = (UserContext)session.getAttribute(SESSION_USER);
            return (String)ctx.get(Context.CREDENTIAL);
        } catch (ContextException ce) {
            return null;
        }
    }


    private void actionList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        User user = Registry.getUserManager().retrieveUser(getUserKey(httpServletRequest.getSession()));
        AddressBook addressBook = user.getAddressBook();

        httpServletRequest.setAttribute(VIEW_ADRESSBOOK, addressBook);

        viewList(httpServletRequest, httpServletResponse);
    }

    private void actionAdd(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        User user = Registry.getUserManager().retrieveUser(getUserKey(httpServletRequest.getSession()));
        AddressBook addressBook = user.getAddressBook();

        String firstName = httpServletRequest.getParameter(KEY_FIRSTNAME);
        String lastName = httpServletRequest.getParameter(KEY_LASTNAME);
        String email = httpServletRequest.getParameter(KEY_EMAIL);

        // add to address book
        Registry.getUserManager().addContact(user, firstName, lastName, email);

        // feed the view
        httpServletRequest.setAttribute(VIEW_ADRESSBOOK, addressBook);
        viewList(httpServletRequest, httpServletResponse);
    }

    private void actionRemove(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        User user = Registry.getUserManager().retrieveUser(getUserKey(httpServletRequest.getSession()));
        AddressBook addressBook = user.getAddressBook();

        // adb entries are firstName.lastName
        String[] ids = httpServletRequest.getParameterValues(KEY_IDS);
        Set contacts = new HashSet();
        for (int i = 0; i < ids.length; i++) {
            System.out.println("  " + ids[i]);
            Contact c = addressBook.findContact(ids[i]);
            contacts.add(c);
        }
        Registry.getUserManager().removeContacts(user, contacts);

        // feed the view
        httpServletRequest.setAttribute(VIEW_ADRESSBOOK, addressBook);
        viewList(httpServletRequest, httpServletResponse);
    }

}
