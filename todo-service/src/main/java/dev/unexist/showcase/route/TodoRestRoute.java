/**
 * @package Showcase-Knative-Camel-Quarkus
 *
 * @file Todo route
 * @copyright 2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/
 
package dev.unexist.showcase.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// camel-k: dependency=mvn:org.apache.camel.quarkus:camel-quarkus-jackson

@ApplicationScoped
public class TodoRestRoute extends RouteBuilder {
    private TodoService todoService = new TodoService();

    @Override
    public void configure() throws Exception {
        restConfiguration().bindingMode(RestBindingMode.json);

        /* Create REST endpoint */
        rest("todo")
            .post("/")
                .consumes("application/json").type(Todo.class)
                .route().bean(todoService, "create(${body})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .endRest()
            .get("/{id}")
                .route().bean(todoService, "findById(${header.id})").endRest()
            .put("/{id}")
                .consumes("application/json").type(Todo.class)
                .route().bean(todoService, "update(${header.id},${body})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .endRest()
            .delete("/{id}")
                .route().bean(todoService, "delete(${header.id})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .endRest()
            .get()
                .route().bean(todoService, "getAll()").endRest();
    }

    public class Todo {
        private int id;
        private String title;
        private String description;
        private Boolean done;

        /**
         * Construct a new {@link Todo}
         *
         * @param  id           Id of the entry
         * @param  title        Title of the entry
         * @param  description  Description of the entry
         * @param  done         Done state of the entry
         **/

        public Todo(int id, String title, String description, Boolean done) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.done = done;
        }

        /**
         * Get id of entry
         *
         * @return Id of the entry
         **/

        public int getId() {
            return id;
        }

        /**
         * Set id of entry
         *
         * @param  id  Id of the entry
         **/

        public void setId(int id) {
            this.id = id;
        }

        /**
         * Get title of the entry
         *
         * @return Title of the entry
         **/

        public String getTitle() {
            return title;
        }

        /**
         * Set title of the entry
         *
         * @param  title  Title of the entry
         **/

        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * Get description of entry
         *
         * @return Description of the entry
         **/

        public String getDescription() {
            return description;
        }

        /**
         * Set description of the entry
         *
         * @param description
         *          Description of the entry
         **/

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Get done state of entry
         *
         * @return Done state of the entry
         **/

        public Boolean getDone() {
            return done;
        }

        /**
         * Set done state of entry
         *
         * @param  done  Done state of the entry
         **/

        public void setDone(Boolean done) {
            this.done = done;
        }
    }

    public class TodoService {
        private TodoRepository todoRepository = new TodoRepository();

        /**
         * Create new {@link Todo} entry and store it in repository
         *
         * @param  todo  A {@link Todo} to add
         *
         * @return Either {@code true} on success; otherwise {@code false}
         **/

        public boolean create(Todo todo) {
            this.todoRepository.add(todo);

            return true;
        }

        /**
         * Update {@link Todo} at with given id
         *
         * @param  id      Id to update
         * @param  values  Values for the entry
         *
         * @return Either {@code true} on success; otherwise {@code false}
         **/

        public boolean update(int id, Todo values) {
            Optional<Todo> todo = this.findById(id);
            boolean ret = false;

            if (todo.isPresent()) {
                values.setId(todo.get().getId());

                ret = this.todoRepository.update(values);
            }

            return ret;
        }

        /**
         * Delete {@link Todo} with given id
         *
         * @param  id  Id to delete
         *
         * @return Either {@code true} on success; otherwise {@code false}
         **/

        public boolean delete(int id) {
            return this.todoRepository.deleteById(id);
        }

        /**
         * Get all {@link Todo} entries
         *
         * @return List of all {@link Todo}; might be empty
         **/

        public List<Todo> getAll() {
            return this.todoRepository.getAll();
        }

        /**
         * Find {@link Todo} by given id
         *
         * @param  id  Id to look for
         *
         * @return A {@link Optional} of the entry
         **/

        public Optional<Todo> findById(int id) {
            return this.todoRepository.findById(id);
        }
    }

    public class TodoRepository {
        private final Logger LOGGER = LoggerFactory.getLogger(TodoRestRoute.class);

        private final List<Todo> list;

        /**
         * Constructor
         **/

        TodoRepository() {
            this.list = new ArrayList<>();
        }

        /**
         * Add {@link Todo} entry to list
         *
         * @param  todo  {@link Todo} entry to add
         *
         * @return Either {@code true} on success; otherwise {@code false}
         **/

        public boolean add(final Todo todo) {
            todo.setId(this.list.size() + 1);

            return this.list.add(todo);
        }

        /**
         * Update {@link Todo} with given id
         *
         * @param  todo  A {@link Todo} to update
         *
         * @return Either {@code true} on success; otherwise {@code false}
         **/

        public boolean update(final Todo todo) {
            boolean ret = false;

            try {
                this.list.set(todo.getId(), todo);

                ret = true;
            } catch (IndexOutOfBoundsException e) {
                LOGGER.warn("update: id={} not found", todo.getId());
            }

            return ret;
        }

        /**
         * Delete {@link Todo} with given id
         *
         * @param  id  Id to delete
         *
         * @return Either {@code true} on success; otherwise {@code false}
         **/

        public boolean deleteById(int id) {
            boolean ret = false;

            try {
                this.list.remove(id);

                ret = true;
            } catch (IndexOutOfBoundsException e) {
                LOGGER.warn("deleteById: id={} not found", id);
            }

            return ret;
        }

        /**
         * Get all {@link Todo} entries
         *
         * @return List of all stored {@link Todo}
         **/

        public List<Todo> getAll() {
            return Collections.unmodifiableList(this.list);
        }

        /**
         * Find {@link Todo} by given id
         *
         * @param  id  Id to find
         *
         * @return A {@link Optional} with the result of the lookup
         **/

        public Optional<Todo> findById(int id) {
            return this.list.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst();
        }
    }
}
