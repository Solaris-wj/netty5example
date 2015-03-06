package io.netty.example.filetransfer2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class GenFile {
	public static void main(String [] args) throws IOException{
		
		File file=new File("C:/1.txt");
		
		FileWriter ofs=new FileWriter(file);
		
		int num=10000;
		
		for(int i = 0 ; i !=num;++i){
			ofs.write(String.valueOf(i)+" ");
		}
		
		ofs.close();
	}
}
