package com.remainz.common.service.proc;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.remainz.common.param.GenericParam;

/**
 * ディレクトリ内のDBテーブル名を探すためのプロシージャです。
 */
public class TableNameSearchProc extends DirProcBase {

	protected void doFileProc(GenericParam input, GenericParam output, String dirPath, File parentDir,
			File currentFile) {

		// ファイル名が "tableNameList.txt" の場合は、何もしない
		if ("tableNameList.txt".equals(currentFile.getName())) {
			return;
		}

		// 入力パラメータが "tableNameList" を持っていない場合は作成する
		ArrayList<LinkedHashMap<String, String>> recordList = output.getRecordList("tableNameList");
		if (recordList == null) {
			recordList = new ArrayList<LinkedHashMap<String, String>>();
			output.putRecordList("tableNameList", recordList);
		}

		// ファイル名から拡張子(".txt")を除外した名前を、テーブル名としてリストに追加する
		String tableName = currentFile.getName().substring(0, currentFile.getName().length() - ".txt".length());
		LinkedHashMap<String, String> record = new LinkedHashMap<String, String>();
		record.put("Tables_in_db", tableName); // MySQLの場合、"Tables_in_[DB名]" (例："Tables_in_jw")となる。
		recordList.add(record);
	}
}
