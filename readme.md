<jdbc:embedded-database id="embeddedDataSource">
	</jdbc:embedded-database>


	<bean id="MediationParserImpl"
		class="eu.trentorise.smartcampus.mediation.engine.MediationParserImpl">
		<property name="dataSource" ref="embeddedDataSource" />
		<property name="urlServermediation" value="${url.mediation.services}" />
		<property name="webappname" value="${webapp.name}" />
	</bean>

	<task:annotation-driven scheduler="taskScheduler" />
	<task:scheduler id="taskScheduler" pool-size="1" />
