package net.explorviz.extension.modeleditor.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import net.explorviz.model.store.Timestamp;
import net.explorviz.server.helper.FileSystemHelper;
import net.explorviz.server.resources.LandscapeResource;
// @Secured
// Add the "Secured" annotation to enable authentication

//@Secured
@Path("landscape")
public class ModelLandscapeResource {

	private final ExtensionAPIImpl api = ExtensionAPI.get();
	static final Logger LOGGER = LoggerFactory.getLogger(LandscapeResource.class.getName());

	private static final String MODEL_REPOSITORY = "modellRepository";
	private static final String MODEL_REPLAY_REPOSITORY = "modellReplayRepository";
	private static Map<String, Timestamp> timestampCache = new HashMap<String, Timestamp>();

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
	@Path("/export-model/{timestamp}")
	public Response getExportLandscape(@PathParam("timestamp") final long timestamp) throws FileNotFoundException {

		final File modelRepository = new File(
				FileSystemHelper.getExplorVizDirectory() + File.separator + MODEL_REPOSITORY);

		// retrieve file from landscape repository with specific timestamp
		final File[] filesWithTimestamp = modelRepository.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File modelRepository, final String filename) {
				return filename.startsWith(Long.toString(timestamp));
			}
		});

		File exportModel;

		if (filesWithTimestamp == null) {
			throw new FileNotFoundException("No model found with timestamp:" + timestamp);
		} else {
			exportModel = new File(filesWithTimestamp[0].getAbsolutePath());
		}

		String encodedModel = "";
		// encode to Base64
		try (FileInputStream streamedModel = new FileInputStream(exportModel)) {
			final byte[] modelData = new byte[(int) exportModel.length()];
			streamedModel.read(modelData);
			encodedModel = Base64.getEncoder().encodeToString(modelData);
		} catch (final IOException ioe) {
			LOGGER.error("error {} in encoding landscape with timestamp {}.", ioe.getMessage(), timestamp);
		}
		// send encoded landscape
		return Response.ok(encodedModel, "*/*").build();
	}

	@Consumes("multipart/form-data")
	@POST
	@Path("/upload-model")
	public Response uploadmodel(@FormDataParam("file") final InputStream uploadedInputStream,
			@FormDataParam("file") final FormDataContentDisposition fileInfo) {

		final String baseFilePath = FileSystemHelper.getExplorVizDirectory() + File.separator;
		final String replayFilePath = baseFilePath + MODEL_REPLAY_REPOSITORY + File.separator;

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

	@Produces("application/vnd.api+json")
	@GET
	@Path("/fill-dropdown")
	public List<Timestamp> getUploadedTimestamps() {
		final File directory = new File(
				FileSystemHelper.getExplorVizDirectory() + File.separator + MODEL_REPLAY_REPOSITORY);
		final File[] fList = directory.listFiles();
		final List<Timestamp> timestamps = new LinkedList<Timestamp>();

		if (fList != null) {
			for (final File f : fList) {
				final String filename = f.getName();

				if (filename.endsWith(".expl")) {
					// first validation check -> filename

					final String timestampAsString = filename.split("-")[0];
					final String activityAsString = filename.split("-")[1].split(".expl")[0];

					Timestamp possibleTimestamp = timestampCache.get(timestampAsString + activityAsString);

					if (possibleTimestamp == null) {

						// new timestamp -> add to cache
						// and initialize ID of entity
						long timestamp;
						long activity;

						try {
							timestamp = Long.parseLong(timestampAsString);
							activity = Long.parseLong(activityAsString);
						} catch (final NumberFormatException e) {
							continue;
						}

						possibleTimestamp = new Timestamp(timestamp, activity);
						possibleTimestamp.initializeID();
						timestampCache.put(timestampAsString + activityAsString, possibleTimestamp);
					}

					timestamps.add(possibleTimestamp);
				}
			}
		}
		return timestamps;
	}
}