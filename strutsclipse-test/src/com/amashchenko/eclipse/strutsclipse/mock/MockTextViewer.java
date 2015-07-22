package com.amashchenko.eclipse.strutsclipse.mock;

import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public class MockTextViewer implements ITextViewer {

	private final IDocument document;

	public MockTextViewer(IDocument document) {
		this.document = document;
	}

	@Override
	public IDocument getDocument() {
		return document;
	}

	@Override
	public void setDocument(IDocument document) {
	}

	@Override
	public void setDocument(IDocument document, int modelRangeOffset,
			int modelRangeLength) {
	}

	@Override
	public StyledText getTextWidget() {
		return null;
	}

	@Override
	public void setUndoManager(IUndoManager undoManager) {
	}

	@Override
	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy,
			String contentType) {
	}

	@Override
	public void setAutoIndentStrategy(IAutoIndentStrategy strategy,
			String contentType) {
	}

	@Override
	public void setTextHover(ITextHover textViewerHover, String contentType) {
	}

	@Override
	public void activatePlugins() {
	}

	@Override
	public void resetPlugins() {
	}

	@Override
	public void addViewportListener(IViewportListener listener) {
	}

	@Override
	public void removeViewportListener(IViewportListener listener) {
	}

	@Override
	public void addTextListener(ITextListener listener) {
	}

	@Override
	public void removeTextListener(ITextListener listener) {
	}

	@Override
	public void addTextInputListener(ITextInputListener listener) {
	}

	@Override
	public void removeTextInputListener(ITextInputListener listener) {
	}

	@Override
	public void setEventConsumer(IEventConsumer consumer) {
	}

	@Override
	public void setEditable(boolean editable) {
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setVisibleRegion(int offset, int length) {
	}

	@Override
	public void resetVisibleRegion() {
	}

	@Override
	public IRegion getVisibleRegion() {
		return null;
	}

	@Override
	public boolean overlapsWithVisibleRegion(int offset, int length) {
		return false;
	}

	@Override
	public void changeTextPresentation(TextPresentation presentation,
			boolean controlRedraw) {
	}

	@Override
	public void invalidateTextPresentation() {
	}

	@Override
	public void setTextColor(Color color) {
	}

	@Override
	public void setTextColor(Color color, int offset, int length,
			boolean controlRedraw) {
	}

	@Override
	public ITextOperationTarget getTextOperationTarget() {
		return null;
	}

	@Override
	public IFindReplaceTarget getFindReplaceTarget() {
		return null;
	}

	@Override
	public void setDefaultPrefixes(String[] defaultPrefixes, String contentType) {
	}

	@Override
	public void setIndentPrefixes(String[] indentPrefixes, String contentType) {
	}

	@Override
	public void setSelectedRange(int offset, int length) {
	}

	@Override
	public Point getSelectedRange() {
		return null;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public void revealRange(int offset, int length) {
	}

	@Override
	public void setTopIndex(int index) {
	}

	@Override
	public int getTopIndex() {
		return 0;
	}

	@Override
	public int getTopIndexStartOffset() {
		return 0;
	}

	@Override
	public int getBottomIndex() {
		return 0;
	}

	@Override
	public int getBottomIndexEndOffset() {
		return 0;
	}

	@Override
	public int getTopInset() {
		return 0;
	}
}
