<?php

//������������ ����� ��������� ������, ����� ��� ������ � ���� ����, ����� �� �������� �� ������������ php � ��, ������ ����� ������ � ����� ������
set_error_handler('err_handler');
function err_handler($errno, $errmsg, $filename, $linenum) {
	$date = date('Y-m-d H:i:s (T)');
	$f = fopen('phpERROR_LOG.txt', 'a');
	if (!empty($f)) {
		$filename  = str_replace($_SERVER['DOCUMENT_ROOT'],'',$filename);
		$err  = "$errmsg = $filename = $linenum\r\n";
		fwrite($f, $err);
		fclose($f);
	}
}

require_once 'connectDB.php';

//�������������� �������:
/*     
 *	get_record - ������ ����� ��������, ������� � ��, ��� "� ������"
 *	put_record_data - �������� ���������� � ��, ��� ���������: id - ������������� 1�, barcode - ��� ���������, codetype - ��� ���������
 *  refusal_of_work - �������� � �������� ��������� "� ������", ��� ��������: id - ������������� 1�
 *  get_cards_count - ������ ���������� ���������� ������
 *  skip_card - ���������� ��������
 */

$answer = array();

if (!connect_OK($error)) 
{
     $answer["success"] = false;
     $answer["reason"] = "DBconnect_error";
	 $answer["error_text"] = $error;
}
else 
{  
    if (filter_input(INPUT_POST,"action") !== null)
    {  
		$answer["success"] = false;	
		//�������� ���������� ����������
		//sleep(2);	

		mysqli_set_charset($link, "utf8");		

		//������ ��������, ������� � �� ��� "� ������"
		if (filter_input(INPUT_POST,"action") === 'get_record')
        {
			$query = "SELECT `id`, `webarticle`, `name`, `catalog`, `count` FROM `bar_codes` WHERE (`barcode` IS NULL OR `barcode` = '') AND `inwork` = 0 AND `worknow` = 1 ORDER BY `catalog`,`name` LIMIT 1";
			$result = mysqli_query($link, $query);
			$records = mysqli_fetch_all($result);
			if (count($records) > 0) {
				$answer["success"] = true;
				$answer["data"] = $records[0];
				$id = $answer["data"][0];			
				
				//������ ��������� "� ������"
				$query = "UPDATE `bar_codes` SET `inwork` = 1 WHERE `id` = '$id'";
				$result = mysqli_query($link, $query);
			} else {
				 $answer["success"] = false;
				 $answer["reason"] = "all_barcodes_scanned";
			}
		}
		
		//������ ���������� �������� � ������
		if (filter_input(INPUT_POST,"action") === 'get_cards_count')
        {
			$query = "SELECT COUNT(*) FROM `bar_codes` WHERE (`barcode` IS NULL OR `barcode` = '') AND `inwork` = 0 AND `worknow` = 1";
			$result = mysqli_query($link, $query);
			$records = mysqli_fetch_all($result);
			$answer["success"] = true;
			$answer["records_count"] = $records[0][0];
		}
		
		//�������� ���������� � ��, ��� ���������: id - ������������� 1�, barcode - ��� ���������, codetype - ��� ���������
		if (
			filter_input(INPUT_POST,"action") === 'put_record_data' && 
			filter_input(INPUT_POST,"id") !== null && 
			filter_input(INPUT_POST,"barcode")!==null &&
			filter_input(INPUT_POST,"author") !== null) 
			//filter_input(INPUT_POST,"imei") !== null && 
			//filter_input(INPUT_POST,"phone_number") !== null)			
		{		    	
			$id = filter_input(INPUT_POST,"id");
			$barcode = filter_input(INPUT_POST,"barcode");
			$codetype = filter_input(INPUT_POST,"codetype");
			$author = filter_input(INPUT_POST,"author");
			
			/*
			$imei = filter_input(INPUT_POST,"imei");
			$phone_number = filter_input(INPUT_POST,"phone_number");		
			
			//��������� ���� �� � ��� ����� ���� � ����
			$query = "SELECT `id` FROM `authors` WHERE `imei` = '$imei'";
			$result = mysqli_query($link, $query);
			$records = mysqli_fetch_all($result);
			
			$author_id = -1;
			if (count($records) > 0) {
				//����� ������������ ����
				$author_id = $records[0][0];
			} else {
				//��������� ����� ������������
				$query = "INSERT INTO `authors` (`imei`, `phone_number`) VALUES ('$imei', '$phone_number')";
				$result = mysqli_query($link, $query);
				
				$author_id = mysqli_insert_id($link);				
				$author_id = 1;
			}
			
			$query = "UPDATE `bar_codes` SET `barcode` = '$barcode', `codetype` = '$codetype', `inwork` = 0, `author_id` = $author_id, `update_time` = now() WHERE `id` = '$id'";
			*/
			
			//���������, ��� � ���� �� ���� ������ ���������
			$query = "SELECT COUNT(*) FROM `bar_codes` WHERE `barcode` = '$barcode' AND `codetype` = '$codetype'";
			$result = mysqli_query($link, $query);	
			$records = mysqli_fetch_all($result);
			if ($records[0][0] == 0) {
				$query = "UPDATE `bar_codes` SET `barcode` = '$barcode', `codetype` = '$codetype', `inwork` = 0, `author` = '$author', `update_time` = now() WHERE `id` = '$id'";		
				$result = mysqli_query($link, $query);
				$answer["success"] = true;
			} else {
				$answer["success"] = false;
				$answer["reason"] = "barcode_is_already_exists";
			}
			
			
			
			
		} else if (filter_input(INPUT_POST,"action") === 'put_record_data'){
			if (filter_input(INPUT_POST,"id") === null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_id";
			}
			if (filter_input(INPUT_POST,"barcode") === null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_barcode";
			}	
			if (filter_input(INPUT_POST,"author") === null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_author";
			}
			/*
			if (filter_input(INPUT_POST,"imei") === null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_imei";
			}
			if (filter_input(INPUT_POST,"phone_number") === null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_phone_number";
			}
			*/			
		}
		
		//�������� � �������� ��������� "� ������", ��� ��������: id - ������������� 1�
		if (filter_input(INPUT_POST,"action") === 'refusal_of_work' && filter_input(INPUT_POST,"id") !== null)
        {
			$id = filter_input(INPUT_POST,"id");
			$query = "UPDATE `bar_codes` SET `inwork` = 0 WHERE `id` = '$id'";
			$result = mysqli_query($link, $query);
			
			$answer["success"] = true;
		} else if (filter_input(INPUT_POST,"action") === 'refusal_of_work'){
			if (filter_input(INPUT_POST,"id") == null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_id";
			}				
		}

		//���������� ��������
		if (filter_input(INPUT_POST,"action") === 'skip_card' && filter_input(INPUT_POST,"id") !== null && filter_input(INPUT_POST,"author") !== null)
        {
			$id = filter_input(INPUT_POST,"id");
			$author = filter_input(INPUT_POST,"author");
			$query = "UPDATE `bar_codes` SET `inwork` = 0, `worknow` = 0, `author` = '$author', `update_time` = now() WHERE `id` = '$id'";
			$result = mysqli_query($link, $query);
			
			$answer["success"] = true;
		} else if (filter_input(INPUT_POST,"action") === 'skip_card'){
			if (filter_input(INPUT_POST,"id") == null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_id";
			}	
			if (filter_input(INPUT_POST,"author") == null) {
				$answer["success"] = false;
				$answer["reason"] = "empty_author";
			}					
		}			       
		
    } else {
        $answer["success"] = false;
        $answer["error"] = "empty_action";
    } 
		       
	//����� ������� 	
    echo json_encode($answer); 
    mysqli_close($link);
}