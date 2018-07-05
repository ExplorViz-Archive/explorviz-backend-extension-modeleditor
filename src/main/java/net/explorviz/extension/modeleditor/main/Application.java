package net.explorviz.extension.modeleditor.main;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import net.explorviz.api.ExtensionAPIImpl;
import net.explorviz.extension.modeleditor.model.DummyModel;
import net.explorviz.extension.modeleditor.model.SubDummyModel;
import net.explorviz.extension.modeleditor.providers.DummyModelProvider;

@ApplicationPath("/extension/dummy")
public class Application extends ResourceConfig {

	public Application() {

		// register the models that you wan't to parse to JSONAPI-conform JSON,
		// i.e. exchange with frontend
		final ExtensionAPIImpl coreAPI = ExtensionAPIImpl.getInstance();

		coreAPI.registerSpecificModel("DummyModel", DummyModel.class);
		coreAPI.registerSpecificModel("SubDummyModel", SubDummyModel.class);

		// register DI
		register(new ExtensionDependencyInjectionBinder());

		// Enable CORS
		register(CORSResponseFilter.class);

		// https://stackoverflow.com/questions/30653012/multipart-form-data-no-injection-source-found-for-a-parameter-of-type-public-ja/30656345
		// register for uploading landscapes
		register(MultiPartFeature.class);

		// register all providers in the given package
		register(DummyModelProvider.class);

		// register all resources in the given package
		packages("net.explorviz.extension.modeleditor.resources");
	}
}
