<%@ page import="aspectwerkz.aosd.addressbook.AddressBook, aspectwerkz.aosd.addressbook.Contact,
                 java.util.Iterator" %><%response.setContentType("text/html");response.setHeader("Cache-Control","no-cache");response.setHeader("Pragma","no-cache");response.setDateHeader ("Expires", -1);%><?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<body>

<a href="demo.do?action=HOME">HOME<a><br/>

<form action="demo.do">
<input type="hidden" name="action" value="REMOVE"/>
<table border="1">
<tr>
    <td>Contact</td>
    <td>&nbsp;</td>
</tr>
<%
    AddressBook adb = (AddressBook) request.getAttribute("adb");
    for (Iterator contacts = adb.getContacts().iterator(); contacts.hasNext();) {
        Contact contact = (Contact) contacts.next();
        %>
        <tr>
            <td><%= contact.toString() %></td>
            <td><input type="checkbox" name="ids" value="<%= contact.getId() %>"/></td>
        </tr>
        <%
    }
%>
</table>
<input type="submit" value="remove"/><br/>
</form>

</body>
</html>