#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Welcome</title>
</head>
<body>
  <p>
  Welcome to Google App Engine for Java!
  </p>
  <p>
  <ul>
    <c:forEach var="message" items="${symbol_dollar}{messages}">
      <li>
        <div><c:out value="${symbol_dollar}{message.text}"/></div>
        <div><a href="index?id=<c:out value="${symbol_dollar}{message.id}"/>">Delete</a></div>
      </li>
    </c:forEach>
  </ul>
  </p>
  <p>
    <form action="index" method="post">
        <input type="text" name="text" />
        <input type="submit" value="Create" />
    </form>
  </p>
</body>
</html>