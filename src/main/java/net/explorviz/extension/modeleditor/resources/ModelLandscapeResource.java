package net.explorviz.extension.modeleditor.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.api.ExtensionAPI;
import net.explorviz.api.ExtensionAPIImpl;
import net.explorviz.model.landscape.Landscape;
import net.explorviz.repository.RepositoryStorage;
import net.explorviz.server.helper.FileSystemHelper;
import net.explorviz.server.resources.LandscapeResource;
// @Secured
// Add the "Secured" annotation to enable authentication
import net.explorviz.server.security.Secured;

@Secured
@Path("landscape")
public class ModelLandscapeResource {

	private final ExtensionAPIImpl api = ExtensionAPI.get();
	static final Logger LOGGER = LoggerFactory.getLogger(LandscapeResource.class.getName());

	private static final String MODEL_REPOSITORY = "modellRepository";

	@PATCH
	@Consumes("application/vnd.api+json")
	@Path("/landscapes/{timestamp}")
	public void saveLandscape(@PathParam("timestamp") final String timestamp, final Landscape landscape) {
		api.saveLandscapeToFile(landscape, MODEL_REPOSITORY);
	}

	@POST
	@Consumes("application/vnd.api+json")
	@Path("/landscapes")
	public void saveLandscape(final Landscape landscape) {
		api.saveLandscapeToFile(landscape, MODEL_REPOSITORY);
	}

	@Produces("*/*")
	@GET
	@Path("/export-model/{fileName}")
	public Response getExportLandscape(@PathParam("fileName") final String fileName) throws FileNotFoundException {

		final String landscapeFolder = FileSystemHelper.getExplorVizDirectory() + File.separator + MODEL_REPOSITORY;

		final Landscape landscapeWithNewIDs = RepositoryStorage.readFromFileGeneric(landscapeFolder,
				fileName + ".expl");

		final byte[] landscapeAsBytes = RepositoryStorage.convertLandscapeToBytes(landscapeWithNewIDs);

		final String encodedLandscape = Base64.getEncoder().encodeToString(landscapeAsBytes);

		// send encoded landscape
		return Response.ok(encodedLandscape, "*/*").build();
	}

	@Consumes("multipart/form-data")
	@POST
	@Path("/upload-model")
	public Response uploadmodel(@FormDataParam("file") final InputStream uploadedInputStream,
			@FormDataParam("file") final FormDataContentDisposition fileInfo) {

		// base64 string in byte Array ohne Datei speichern

		// final byte[] byteLandscape = (byte[]) uploadedInputStream;

		// RepositoryStorage byteToLandscape
		// final Landscape newAndBetterLandscape =
		// RepositoryStorage.bytesToLandscape(byteLandscape);

		// neues landscape in datei speichern

		final String baseFilePath = FileSystemHelper.getExplorVizDirectory() + File.separator;
		final String replayFilePath = baseFilePath + MODEL_REPOSITORY + File.separator;

		new File(replayFilePath).mkdir();
		final File objFile = new File(replayFilePath + fileInfo.getFileName());
		if (objFile.exists()) {
			objFile.delete();

		}
		saveToFile(uploadedInputStream, replayFilePath + fileInfo.getFileName());

		return Response.ok().build();
	}

	private void saveToFile(final InputStream uploadedInputStream, final String uploadedFileLocation) {
		// decode and save landscape
		try (InputStream base64is = Base64.getDecoder().wrap(uploadedInputStream)) {
			int len = 0;
			OutputStream out = null;
			final byte[] bytes = new byte[1024];
			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((len = base64is.read(bytes)) != -1) {
				out.write(bytes, 0, len);
			}
			out.flush();
			out.close();
		} catch (final IOException e1) {
			LOGGER.error(
					"Replay model could not be saved to modelreplay repository. Error {} occured. With stacktrace {}",
					e1.getMessage(), e1.getStackTrace());

		}

	}

	@GET
	@Produces("application/json")
	@Path("/fill-dropdown")
	public Response getUploadedTimestamps() {
		final File directory = new File(FileSystemHelper.getExplorVizDirectory() + File.separator + MODEL_REPOSITORY);
		final File[] fList = directory.listFiles();
		final List<String> timestamps = new LinkedList<String>();

		if (fList != null) {
			for (final File f : fList) {
				final String filename = f.getName();

				if (filename.endsWith(".expl")) {
					// first validation check -> filename
					final String timestampAsString = filename.split(".expl")[0];

					timestamps.add(timestampAsString);
				}
			}
		}
		return Response.ok(timestamps, "application/json").build();
	}

	@Produces("application/vnd.api+json")
	@GET
	@Path("/modelLandscape/{timestamp}")
	public Landscape getModelLandscape(@PathParam("timestamp") final long fileName) {
		final File directory = new File(FileSystemHelper.getExplorVizDirectory() + File.separator + MODEL_REPOSITORY);
		final File[] fList = directory.listFiles();
		final String strLong = Long.toString(fileName);

		if (fList != null) {
			for (final File f : fList) {
				final String filename = f.getName();
				if (filename.endsWith(".expl") && filename.split("-")[0].equals(strLong)) {
					// first validation check -> filename
					return api.getLandscape(fileName, MODEL_REPOSITORY);
				}
			}
		} else {
			// error modelReplayRepository is empty
		}
		// error handling should throw a well produced and talkative error at this point
		return null;
	}

}