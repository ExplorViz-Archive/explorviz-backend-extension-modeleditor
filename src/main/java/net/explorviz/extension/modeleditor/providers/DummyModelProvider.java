package net.explorviz.extension.modeleditor.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import net.explorviz.extension.modeleditor.model.DummyModel;

public class DummyModelProvider implements MessageBodyReader<DummyModel>, MessageBodyWriter<DummyModel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DummyModelProvider.class);

	private final ResourceConverter converter;

	@Inject
	public DummyModelProvider(final ResourceConverter converter) {
		this.converter = converter;
	}

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void writeTo(final DummyModel model, final Class<?> type, final Type genericType,
			final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
			final OutputStream entityStream) throws IOException, WebApplicationException {

		final JSONAPIDocument<DummyModel> document = new JSONAPIDocument<DummyModel>(model);

		try {
			entityStream.write(this.converter.writeDocument(document));
		} catch (final DocumentSerializationException e) {
			LOGGER.error("Error when serializing DummyModel: ", e);
		} finally {
			entityStream.flush();
			entityStream.close();
		}

	}

	@Override
	public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public DummyModel readFrom(final Class<DummyModel> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream)
			throws IOException, WebApplicationException {
		return this.converter.readDocument(entityStream, type).get();
	}

}
