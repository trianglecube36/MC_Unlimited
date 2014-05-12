package io.github.trianglecube36.unlimited;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class ULoadingPlugin  implements IFMLLoadingPlugin{
	
	public static File jarfile;
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{UTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		jarfile = (File) data.get("coremodLocation");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
