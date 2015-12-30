package uk.co.javahelp.maven.plugin.fitnesse.mojo;

public class TestFitNesseMojo extends AbstractFitNesseMojo {

    public Launch[] calledWith = null;

	public TestFitNesseMojo() {
		this.launches = new Launch[0];
	}

	@Override
	protected void executeInternal(Launch... launches) {
		this.calledWith = launches;
	}
}
