/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;


/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 30, 2009
 */
public interface ReasoningEvent {

	public EVENT_TYPES getType();
	public Object[] getParameters();
	public void setParameters(Object[] params);
	
	class SimpleEventImpl implements ReasoningEvent {

		private EVENT_TYPES m_type =  null;
		private Object[] m_params = null;
		
		public SimpleEventImpl(EVENT_TYPES eventType) {
			
			m_type = eventType;
		}
		
		@Override
		public Object[] getParameters() {

			return m_params;
		}
		
		public void setParameters(Object[] params) {
			
			m_params = params;
		}

		@Override
		public EVENT_TYPES getType() {
			
			return m_type;
		}
	}
}
