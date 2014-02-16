import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class JCF {
	
	public static void main(String args[]){
		if(args.length == 0){
			System.out.println("Please enter absolute path of directory with jars");
			return;
		}
		File dir = new File(args[0]);
		if(!dir.isDirectory()){
			System.out.println("Please enter absolute path of directory with jars");
			return;
		}
		//initialize 
		HashMap<String, Set<JarInfo>> classMap = new HashMap<String, Set<JarInfo>>(); 
		
		
		for(File f : dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(!pathname.isDirectory() && pathname.getName().endsWith(".jar")){
					return true;
				}
				return false;
			}
		})){
			try {
				JarFile jar = new JarFile(f);
				Enumeration<JarEntry> jarEntries = jar.entries();
				while(jarEntries.hasMoreElements()){
					JarEntry jarElem = jarEntries.nextElement();
					if(jarElem.getName().endsWith(".class")){
						insertInMap(jarElem.getName(), classMap, f.getName(), jarElem.getSize());
					}
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		System.out.println("Found " + classMap.size() + " classes");
		//remove the ones which appear only in one jar:
		
		//print conflict entries:
		for(Entry<String, Set<JarInfo>> entry : classMap.entrySet()){
			if(entry.getValue().size() > 1){
				StringBuilder sb = new StringBuilder();
				sb.append("Class "+ entry.getKey() + " was found in the following jars:");
				boolean first= true;
				boolean sameSize = true;
				long size = 0;
				for(JarInfo jar : entry.getValue()){
					if(first){
						first=false;
						size=jar.getClassSize();
					}else{
						sb.append(", ");
						if(size != jar.getClassSize()){
							sameSize = false;
						}
					}
					sb.append(jar.getJarName());
				}
				if(sameSize){
					System.out.println(sb.toString());
				} else {
					sb.append(" with different classfile sizes!");
					System.err.println(sb.toString());
				}
				
			}
		}
	}
	
	private static void insertInMap(String name, HashMap<String, Set<JarInfo>> map, String jarName, long classSize){
		String keyName = name;
		if(keyName.endsWith(".class")){
			keyName = keyName.substring(0, keyName.length()-6);
		}
		JarInfo info = new JarInfo();
		info.setClassSize(classSize);
		info.setJarName(jarName);
		if(map.containsKey(keyName)){
			map.get(keyName).add(info);
		}else{
			HashSet<JarInfo> content = new HashSet<JarInfo>();
			content.add(info);
			map.put(keyName, content);
		}
	}

}
