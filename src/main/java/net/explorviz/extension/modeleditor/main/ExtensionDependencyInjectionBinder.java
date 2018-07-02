package net.explorviz.extension.modeleditor.main;

public class ExtensionDependencyInjectionBinder extends net.explorviz.server.main.DependencyInjectionBinder {

	@Override
	public void configure() {
		// call core DI, so we can use it
		// in this extension if necessary
		super.configure();

		// bind your possible injections here
		// this.bind(X.class).to(X.class).in(Singleton.class)
	}
}
