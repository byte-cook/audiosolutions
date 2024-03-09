package de.kobich.audiosolutions.frontend.common.ui;

import java.util.HashSet;
import java.util.Set;

import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;

public abstract class AbstractTableTreeNode<T, C extends AbstractTableTreeNode<?, ?>> implements IFileDescriptorsSource {
	private T content;
	private Set<C> children;
	
	public AbstractTableTreeNode(T content) {
		this.content = content;
		this.children = new HashSet<C>();
	}

	/**
	 * @return the children
	 */
	public Set<C> getChildren() {
		return children;
	}
	
	public boolean hasChild(C child) {
		return children.contains(child);
	}

	/**
	 * @return the content
	 */
	public T getContent() {
		return content;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractTableTreeNode other = (AbstractTableTreeNode) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		}
		else if (!content.equals(other.content))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getContent().toString();
	}
}
