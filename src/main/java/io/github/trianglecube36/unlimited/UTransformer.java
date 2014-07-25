package io.github.trianglecube36.unlimited;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.IClassTransformer;

public class UTransformer implements IClassTransformer{

	public static HashMap<String, ZipEntry> mapToReplace = null;
	public static ZipFile jarFileZip = null;
	
	public static void setUp(){
		try {
			jarFileZip = new ZipFile(ULoadingPlugin.jarfile);
			mapToReplace = new HashMap<String, ZipEntry>();
			//ZipEntry over = jarFileZip.getEntry("overrideBin");
			Enumeration<? extends ZipEntry> entrys = jarFileZip.entries();
			while(entrys.hasMoreElements()){
				ZipEntry en = (ZipEntry) entrys.nextElement();
				String name = en.getName();
				if(!en.isDirectory() && name.endsWith(".class")){
					String s = name.substring(0, name.length() - 6).replace('/', '.'); // strip .class
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
		if(!deobfClazzName.startsWith("net.minecraft")){ // note: this includes net.minecraftforge - mite need to smash up some stuff in there
			return oldData;
		}
		if(mapToReplace == null){
			setUp();
		}
		ZipEntry en = mapToReplace.get(deobfClazzName);
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
