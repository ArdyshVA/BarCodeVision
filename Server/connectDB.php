<?php
//���� ���������� ��������� ������� � ����, �� ��������� ����� ����:
$mysql_host = 'localhost'; // sql ������
$mysql_user = 'root'; // ������������
$mysql_password = 'HardPass123'; // ������
$mysql_database = 'BarCodesDB'; // ��� ���� ������ 

$link = mysqli_connect($mysql_host,$mysql_user,$mysql_password,$mysql_database);

function connect_OK(&$problem)
{
    if (mysqli_connect_errno())
    {    
		$problem = '������ � ����������� � �� ('.mysqli_connect_errno().'):'.mysqli_connect_error();
		echo ($problem);
		//exit();
		return false;
    }
    return true;
}