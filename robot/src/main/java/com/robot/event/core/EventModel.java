package com.robot.event.core;

/**
 * 事件数据模型
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-30
 */
public class EventModel implements java.io.Serializable  {
	
	private static final long serialVersionUID = -6215104676746611824L;

	private Object model;		// 要传递的值
	private String key;				// 对应的Listener类里getKey()的值，用于识别Listener
	private boolean async = false;		// 同步或异步执行，true时为异步，默认为false, 同步

	public static class Builder {
		private Object value;
		private String key;
		private boolean async;

		public Builder key(String key) {
			this.key = key;
			return this;
		}
		public Builder value(Object value) {
			this.value = value;
			return this;
		}
		public Builder isSync(boolean async) {
			this.async = async;
			return this;
		}

		public EventModel build() {
			return new EventModel(value, key, async);
		}
	}

	public long getCurrenttime() {
		return System.currentTimeMillis();
	}

	public Object getModel() {
		return model;
	}

	public String getKey() {
		return key;
	}

	public boolean isAsync() {
		return async;
	}

	public EventModel(Object model, String key, boolean async) {
		super();
		this.model = model;
		this.key = key;
		this.async = async;
	}

	@Override
	public String toString() {
		return "DuangEventModel [model=" + model + ", key=" + key +  ", async=" + async +"   CurrentTime=" + getCurrenttime() + "]";
	}


}
