package org.ideademo.pawz.pages.pdf;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.StreamResponse;


public class Index {
	@InjectPage
	private org.ideademo.pawz.pages.Index index;
	
	public StreamResponse onActivate()
    {
		return index.onSelectedFromPdf();
    }
}

