package aspectwerkz.aosd.user;

import aspectwerkz.aosd.addressbook.Contact;

import java.util.Set;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface UserManager {

    public User retrieveUser(String userId);

    public Contact addContact(User user, String firstName, String lastName, String email);

    public User removeContacts(User user, Set contacts);

}
