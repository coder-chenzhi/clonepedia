package clonepedia.model.viewer.comparator;

import java.util.Comparator;

import clonepedia.model.viewer.ClonePatternGroupWrapper;
import clonepedia.model.viewer.IContainer;
import clonepedia.model.viewer.TopicWrapper;

public class AlphabetDescComparator implements Comparator<IContainer> {

	@Override
	public int compare(IContainer arg0, IContainer arg1) {
		if(arg0 instanceof TopicWrapper){
			TopicWrapper topic0 = (TopicWrapper)arg0;
			TopicWrapper topic1 = (TopicWrapper)arg1;
			return topic1.getTopic().getTopicString().compareToIgnoreCase(topic0.getTopic().getTopicString());
		}
		else if(arg0 instanceof ClonePatternGroupWrapper){
			ClonePatternGroupWrapper pattern0 = (ClonePatternGroupWrapper)arg0;
			ClonePatternGroupWrapper pattern1 = (ClonePatternGroupWrapper)arg1;
			try {
				return pattern1.getReverseEpitomise().compareTo(pattern0.getReverseEpitomise());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

}
