<%@ page %><%response.setContentType("text/html");response.setHeader("Cache-Control","no-cache");response.setHeader("Pragma","no-cache");response.setDateHeader ("Expires", -1);%><?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<body>
<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
        %><b>Error:</b><%= error %><br/><br/><%
    }
%>

There is <b><%= request.getAttribute("adb_count") %></b> contacts in the address book.<br/>
<a href="demo.do?action=LIST"/>Show contacts</a><br/>
<a href="add.jsp"/>Add contact</a><br/>
<br/>
<a href="demo.do?action=LOGOUT"/>Logout</a><br/>

</body>
</html>


