<%@ page import="aspectwerkz.aosd.addressbook.AddressBook, aspectwerkz.aosd.addressbook.Contact,
                 java.util.Iterator" %><%response.setContentType("text/html");response.setHeader("Cache-Control","no-cache");response.setHeader("Pragma","no-cache");response.setDateHeader ("Expires", -1);%><?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<body>

<a href="demo.do?action=HOME">HOME<a><br/>

<form action="demo.do" method="GET">
<input type="hidden" name="action" value="ADD"/>
First name: <input type="text" name="fn"><br/>
Last name: <input type="text" name="ln"><br/>
email: <input type="text" name="em"><br/>
<input type="submit" value="Add"/>
</form>

</body>
</html>
