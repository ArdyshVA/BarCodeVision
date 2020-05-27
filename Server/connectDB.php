<?php

$mysql_host = 'localhost'; // sql сервер
$mysql_user = 'root'; // пользователь
$mysql_password = ''; // пароль
$mysql_database = 'BarCodes'; // имя базы данных 

$link = mysqli_connect($mysql_host,$mysql_user,$mysql_password,$mysql_database);

function connect_OK(&$problem)
{
    if (mysqli_connect_errno())
    {    
		$problem = 'ошибка в подключении к БД ('.mysqli_connect_errno().'):'.mysqli_connect_error();
		echo ($problem);
		//exit();
		return false;
    }
    return true;
}