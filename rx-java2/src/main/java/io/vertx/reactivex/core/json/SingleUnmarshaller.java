package io.vertx.reactivex.core.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.io.IOException;

import static java.util.Objects.nonNull;

/**
 * An operator to unmarshall json to pojos.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SingleUnmarshaller<T, B> implements SingleTransformer<B, T> {

  private final java.util.function.Function<B, Buffer> unwrap;
  private final Class<T> mappedType;
  private final TypeReference<T> mappedTypeRef;
  private final ObjectMapper mapper;


  public SingleUnmarshaller(java.util.function.Function<B, Buffer> unwrap, Class<T> mappedType) {
    this.unwrap = unwrap;
    this.mappedType = mappedType;
    this.mappedTypeRef = null;
    this.mapper = Json.mapper;
  }

  public SingleUnmarshaller(java.util.function.Function<B, Buffer> unwrap, TypeReference<T> mappedTypeRef) {
    this.unwrap = unwrap;
    this.mappedType = null;
    this.mappedTypeRef = mappedTypeRef;
    this.mapper = Json.mapper;
  }

  @Override
  public SingleSource<T> apply(@NonNull Single<B> upstream) {
    Single<Buffer> unwrapped = upstream.map(unwrap::apply);
    Single<T> unmarshalled = unwrapped.flatMap(buffer -> {
      try {
        T obj = nonNull(mappedType) ? mapper.readValue(buffer.getBytes(), mappedType) :
          mapper.readValue(buffer.getBytes(), mappedTypeRef);
        return Single.just(obj);
      } catch (IOException e) {
        return Single.error(e);
      }
    });
    return unmarshalled;
  }
}
