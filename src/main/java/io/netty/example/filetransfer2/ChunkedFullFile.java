package io.netty.example.filetransfer2;

import java.io.File;
import java.io.IOException;

import io.netty.handler.stream.ChunkedFile;

public class ChunkedFullFile extends ChunkedFile {

	String fileName=null;
	public ChunkedFullFile(String fileName, File file) throws IOException {
		super(file);
		// TODO Auto-generated constructor stub
		this.fileName=fileName;
	}
	

}
