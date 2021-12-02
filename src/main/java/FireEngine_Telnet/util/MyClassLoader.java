package FireEngine_Telnet.util;

public class MyClassLoader {
	private Loader loader;

	public MyClassLoader() {
		loader = new Loader(this.getClass().getClassLoader());
	}

	private class Loader extends ClassLoader {

		public Loader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return super.loadClass(name);
		}

	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loader.loadClass(name);
	}
}