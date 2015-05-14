package example.imagetaskgang.servermodel;

import java.util.List;

public class ServerResponse {
	
	public List<FilterData> filterList;
	
	public ServerResponse(List<FilterData> fList) {
		this.filterList = fList;
	}
}