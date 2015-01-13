package ccdemon.test.model;

import java.util.ArrayList;

import ccdemon.model.ConfigurationPoint;

public class CPWrapperList {
	private ArrayList<CPWrapper> list = new ArrayList<>();

	/**
	 * @param list
	 */
	public CPWrapperList(ArrayList<CPWrapper> list) {
		super();
		this.list = list;
	}

	/**
	 * @return the list
	 */
	public ArrayList<CPWrapper> getList() {
		return list;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(ArrayList<CPWrapper> list) {
		this.list = list;
	}
	
	public ArrayList<ConfigurationPoint> getConfigurationPoints(){
		ArrayList<ConfigurationPoint> pointList = new ArrayList<>();
		for(CPWrapper wrapper: this.list){
			pointList.add(wrapper.getPoint());
		}
		return pointList;
	}
}
