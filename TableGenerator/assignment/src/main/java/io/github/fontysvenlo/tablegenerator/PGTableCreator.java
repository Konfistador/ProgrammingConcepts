package io.github.fontysvenlo.tablegenerator;

import io.github.fontysvenlo.annotations.Default;
import io.github.fontysvenlo.annotations.NotNull;
import io.github.fontysvenlo.annotations.Check;
import io.github.fontysvenlo.annotations.ID;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Create table defintion for a class.
 *
 * @param <E> type type of the entity
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public class PGTableCreator<E> implements TableCreator<E> {

    final Class<E> entityType;

    public PGTableCreator(Class<E> entityType) {
        this.entityType = entityType;
    }

    /**
     * Create the table definition from the entityType
     *
     * @param out The Appendable where all lines of the table definition should be appended to
     * @throws IOException
     */
    @Override
    public void createTable(Appendable out) throws IOException {
        out.append("CREATE TABLE ").append(entityType.getSimpleName().toLowerCase()).append("s");
        out.append(" (\n");
        Field[] declaredFields = entityType.getDeclaredFields();
        List<String> outLines = new ArrayList<>();
        for (Field field : declaredFields) {
            StringBuilder ol = new StringBuilder();
            String pgType = TypeMappings.getPGTypeName(field.getType());
            String declaration = processAnnotations(pgType, field);
            ol.append("    ")
                    .append(declaration);
            outLines.add(ol.toString());

        }
        String body = outLines.stream().collect(joining("," + System.lineSeparator()));
        out.append(body)
                .append(System.lineSeparator())
                .append(");")
                .append(System.lineSeparator())
                .append(System.lineSeparator());


    }


    /**
     * Process the name and annotations on the field to a PostgreSQL column definition.
     *
     * @param pgTypeName name of the field/ column
     * @param field      the java field definition, which may have annotations to be processed.
     * @return the column definition
     */
    String processAnnotations(String pgTypeName, Field field) {
        StringBuilder output = new StringBuilder();
        output.append(pgTypeName);
        output.append(" ");
        output.append(field.getName());
        output.append(" ");
        var annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof NotNull) {
                output.append("NOT NULL");
            } else if (annotation instanceof Default) {
                output.append(" DEFAULT (").append(((Default) annotation).value()).append(")");
            } else if (annotation instanceof ID) {
                if (field.getType() == int.class) pgTypeName = "SERIAL";
                if (field.getType() == long.class) pgTypeName = "BIGSERIAL";
                output.append("PRIMARY KEY");
            } else if (annotation instanceof Check) {
                output.append(" CHECK (").append(((Check) annotation).value()).append(")");
            }
        }
        return output.toString();
    }
}
