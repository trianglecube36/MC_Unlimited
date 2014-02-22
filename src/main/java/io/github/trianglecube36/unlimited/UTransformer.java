package io.github.trianglecube36.unlimited;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.IClassTransformer;

public class UTransformer implements IClassTransformer{

	public static HashMap<String, ZipEntry> mapToReplace = null;
	public static ZipFile jarFileZip;
	
	public static void setUp(){
		try {
			jarFileZip = new ZipFile(ULoadingPlugin.jarfile);
			mapToReplace = new HashMap<String, ZipEntry>();
			ZipEntry over = jarFileZip.getEntry("overrideBin");
			Enumeration entrys = jarFileZip.entries();
			while(entrys.hasMoreElements()){
				ZipEntry en = (ZipEntry) entrys.nextElement();
				String name = en.getName();
				if(!en.isDirectory() && name.startsWith("overrideBin")){
					String s = name.substring(12, name.length() - 6).replace('/', '.');
					mapToReplace.put(s, en);
				}
			}
		} catch (Exception e) {
			System.err.println("Unlimited: ERROR: load file from jar! jar file is messed up!");
			e.printStackTrace();
		}
	}
	
	@Override
	public byte[] transform(String Name, String deobfClazzName, byte[] oldData) {
		if(mapToReplace == null){
			setUp();
		}
		ZipEntry en = mapToReplace.get(deobfClazzName.subSequence(deobfClazzName.lastIndexOf('.', deobfClazzName.length()) + 1, deobfClazzName.length()));
		if(en != null){
			byte[] data = null;
			try {
				InputStream in = jarFileZip.getInputStream(en);
				data = new byte[(int) en.getSize()];
				
				int i = 0;
				int l = data.length;
				int v;
				while(i < l){
					v = in.read();
					if(v == -1){
						System.err.println("Unlimited: END OF FILE (or somthing)");
						System.err.println("Unlimited: i = " + i);
						break;
					}else{
						data[i] = (byte)v;
						i++;
					}
				}
			
				in.close();
				System.out.println("Unlimited: RAPLACED BASE CLASS: " + deobfClazzName);
			} catch (IOException e) {
				System.err.println("Unlimited: ERROR: load file from jar! jar file is messed up!");
				e.printStackTrace();
			}
			return data;
		}else{
			return oldData;
		}
	}
}
