<?php

//регистрируем новый слушатель ошибок, пишем все ошибки в свой файл, чтобы не зависить от конфигурации php и тд, всегда иметь доступ к логам ошибок
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

//поддерживаемые команды:
/*     
 *	get_record - выдать новую карточку, пометив в бд, как "В работе"
 *	put_record_data - положить информацию в бд, доп параметры: id - идентификатор 1С, barcode - код штрихкода, codetype - тип штрихкода
 *  skip_card - пропустить карточку
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
		//эмуляция медленного соединения
		//sleep(2);	

		mysqli_set_charset($link, "utf8");		

		//выдать карточку, пометив в бд как "В работе"
		if (filter_input(INPUT_POST,"action") === 'get_record')
        {
			$query = "SELECT `id`, `webarticle`, `name`, `catalog`, `count` FROM `bar_codes` WHERE (`barcode` IS NULL OR `barcode` = '') AND `inwork` = 0 AND `worknow` = 1 ORDER BY `catalog`,`name` LIMIT 1";
			$result = mysqli_query($link, $query);
			$records = mysqli_fetch_all($result);
			if (count($records) > 0) {
				$answer["success"] = true;
				$answer["data"] = $records[0];
				$id = $answer["data"][0];			
				
				//Ставим состояние "В работе"
				$query = "UPDATE `bar_codes` SET `inwork` = 1 WHERE `id` = '$id'";
				$result = mysqli_query($link, $query);
			} else {
				 $answer["success"] = false;
				 $answer["reason"] = "all_barcodes_scanned";
			}
		}
		
		//положить информацию в бд, доп параметры: id - идентификатор 1С, barcode - код штрихкода, codetype - тип штрихкода
		if (
			filter_input(INPUT_POST,"action") === 'put_record_data' && 
			filter_input(INPUT_POST,"id") !== null && 
			filter_input(INPUT_POST,"barcode")!==null &&
			filter_input(INPUT_POST,"author") !== null) 		
		{		    	
			$id = filter_input(INPUT_POST,"id");
			$barcode = filter_input(INPUT_POST,"barcode");
			$codetype = filter_input(INPUT_POST,"codetype");
			$author = filter_input(INPUT_POST,"author");	
			
			//проверяем, что в базе не было такого штрихкода
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
		}		

		//пропустить карточку
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
		       
	//ответ сервера 	
    echo json_encode($answer); 
    mysqli_close($link);
}