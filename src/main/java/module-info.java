module agents {
	requires jade;
	requires spring.webmvc;
	requires spring.web;
	requires spring.boot;
	requires spring.context;
	requires spring.boot.autoconfigure;
	requires spring.boot.starter;
	requires spring.boot.starter.tomcat;
	requires com.fasterxml.jackson.databind;

	exports agents;
	exports agents.product;
	exports agents.managers;
	exports agents.workers.assemblers;
	exports agents.workers.machines;
	exports agents.workers;
	exports agents.supervisor;
	exports agents.utils;

	opens agents.product;
	opens agents to spring.core;
	opens agents.workers.assemblers to com.fasterxml.jackson.databind;
    opens agents.workers to jade;
}