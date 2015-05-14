package example.imagetaskgang.servermodel;

import java.util.List;

public class FilterData {

	public String filterName;
	public List<ImageData> imageData;
	
	public FilterData(String name, List<ImageData> data) {
        this.filterName = name;
        this.imageData = data;
    }
}
