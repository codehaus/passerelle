package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.SearchPredicate;
import org.jdesktop.swingx.search.PatternModel;

import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.definition.HMIDefinition;
import com.isencia.passerelle.hmi.form.CheckableComponent;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.attributes.TextAttribute;

/**
 * 
 * @author ADIOUF
 * 
 */
public class CheckTree extends JPanel {

	private final Map<Integer, DefaultMutableTreeNode> nodesMap = new HashMap<Integer, DefaultMutableTreeNode>();

	boolean notChild = false;

	DefaultMutableTreeNode node;
	DefaultMutableTreeNode currentNode;

	private CompoundHighlighter tableHighlighters;
	private ColorHighlighter matchHighlighter;
	private PatternModel patternModel;

	CompositeActor model;
	HMIDefinition modelParameterFilterConfig;
	List<Component> filters = new ArrayList<Component>();
	LinkedList<Boolean> filtersSave = new LinkedList<Boolean>();

	HashMap<String, String> labels = new HashMap<String, String>();
	MyTreeCellEditor treeEditor;
	private static final long serialVersionUID = 1L;

	private DefaultTreeModel treeModel;
	private JXTree tree;
	private JScrollPane scrollPane;

	private JPanel searchTextPanel;
	private JTextField searchText;
	private JLabel searchTextLabel;

	int index = 0;
	int indexChekable = 0;
	int paramCount = 0;

	public CheckTree() {
		final TreeNode root = makeSampleTree();
		initTree(root, true, Integer.MAX_VALUE);
	}

	/**
	 * 
	 * @param rootName
	 * @param showRoot
	 * @param checkable
	 * @param maxLeafSelect
	 * @param parameter
	 * @param model
	 * @param parent
	 * @param modelParameterFilterConfig
	 */
	public CheckTree(final String rootName, final boolean showRoot,
			final boolean checkable, final Integer maxLeafSelect,
			final CompositeActor model,
			final HMIDefinition modelParameterFilterConfig) {
		this.model = model;
		this.modelParameterFilterConfig = modelParameterFilterConfig;
		setTree(rootName, showRoot, checkable, maxLeafSelect);
	}

	/**
	 * 
	 * @param rootName
	 * @param showRoot
	 * @param checkable
	 * @param maxLeafSelect
	 * @param parameter
	 * @param model
	 * @param parent
	 * @param modelParameterFilterConfig
	 */
	public void setTree(final String rootName, final boolean showRoot,
			final boolean checkable, final Integer maxLeafSelect) {
		final TreeNode root = makeTree(rootName, checkable);
		initTree(root, showRoot, maxLeafSelect);
	}

	/**
	 * 
	 * @param treeNode
	 * @param showRoot
	 * @param maxLeafSelect
	 */
	private void initTree(final TreeNode treeNode, final boolean showRoot,
			final Integer maxLeafSelect) {

		removeAll();
		tableHighlighters = new CompoundHighlighter();
		matchHighlighter = new ColorHighlighter(HighlightPredicate.NEVER, null,
				Color.red);
		tableHighlighters.addHighlighter(matchHighlighter, true);
		treeModel = new DefaultTreeModel(treeNode);
		tree = new JXTree(treeModel);
		tree.setLargeModel(true);
		tree.setHighlighters(tableHighlighters);
		tree.setEditable(true);
		tree.setRootVisible(showRoot);
		tree.setRolloverEnabled(true);
		tree.setCellRenderer(new MyTreeCellRender());

		treeEditor = new MyTreeCellEditor(tree, maxLeafSelect);
		tree.setCellEditor(treeEditor);
		treeEditor.setLabels(labels);

		// create searchText
		searchText = new JTextField(20);

		bindPatternModel();

		scrollPane = new JScrollPane(tree);
		searchTextPanel = new JPanel();
		searchTextLabel = new JLabel("Search");
		searchTextPanel.add(searchTextLabel);
		searchTextPanel.add(searchText);

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(searchTextPanel, BorderLayout.SOUTH);

	}

	/**
	 * G�nerate default Tree
	 * 
	 * @return TreeNode
	 */
	private TreeNode makeSampleTree() {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				new TreeDataObject("root", TreeDataObject.CHECKABLE));

		DefaultMutableTreeNode lvl1;
		DefaultMutableTreeNode lvl2;
		DefaultMutableTreeNode lvl3;

		for (int i = 1; i < 3; i++) {
			lvl1 = new DefaultMutableTreeNode(new TreeDataObject("node_" + i,
					i, "Cat1", 0, TreeDataObject.CHECKABLE));
			root.add(lvl1);
			for (int j = 1; j < 5; j++) {
				lvl2 = new DefaultMutableTreeNode(new TreeDataObject("node_"
						+ i + "_" + j, i * 10 + j, "Cat2", i,
						TreeDataObject.CHECKABLE));
				lvl1.add(lvl2);
				for (int k = 1; k < 5; k++) {
					lvl3 = new DefaultMutableTreeNode(new TreeDataObject(
							"leef_" + i + "_" + j + "_" + k, i * 100 + j * 10
									+ k, "Cat3", i * 10 + j,
							TreeDataObject.CHECKABLE));
					lvl2.add(lvl3);
				}
			}
		}
		return root;
	}

	/**
	 * 
	 * @param rootName
	 * @param checkable
	 * @param parameters
	 * @param model
	 * @param parent
	 * @param modelParameterFilterConfig
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private TreeNode makeTree(final String rootName, final boolean checkable) {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				new TreeDataObject(rootName, checkable));
		currentNode = root;

		if (model != null) {
			nodesMap.clear();

			// annotations filtering
			final List<TextAttribute> annotations = model
					.attributeList(TextAttribute.class);
			paramCount = buildAnnotationFilter(model, paramCount, annotations,
					root);

			// Top-level parameters ("on the model canvas")
			final List<Parameter> parameters2 = model
					.attributeList(Parameter.class);
			paramCount = buildParameterFilter(model, paramCount, parameters2,
					model, root);

			// Director parameters
			final Director d = model.getDirector();
			final List<Parameter> parameters = d.attributeList(Parameter.class);
			paramCount = buildParameterFilter(model, paramCount, parameters, d,
					root);

			paramCount = addParameterFiltersForComposite(model, model,
					paramCount, root);

			for (final Object element : filters) {
				final CheckableComponent filter = (CheckableComponent) element;

				if (filter.getSubject() instanceof Parameter) {
					final Parameter p = (Parameter) filter.getSubject();

					if (filtersSave.get(indexChekable)) {
						selectItem(indexChekable + " " + p.getName(), root);
					}

				} else if (filter.getSubject() instanceof TextAttribute) {
					final TextAttribute p = (TextAttribute) filter.getSubject();

					if (filtersSave.get(indexChekable)) {
						selectItem(indexChekable + " " + p.getName(), root);
					}
				}
				indexChekable++;
			}
		}
		return root;
	}

	/**
	 * 
	 * @param model
	 * @param parent
	 * @param paramCount
	 * @param rootNode
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int addParameterFiltersForComposite(final CompositeActor model,
			final CompositeActor parent, int paramCount,
			final DefaultMutableTreeNode rootNode) {
		List parameters;
		final List actors = parent.entityList(Actor.class);
		for (final Iterator iter = actors.iterator(); iter.hasNext();) {
			final Entity a = (Entity) iter.next();
			parameters = a.attributeList(Parameter.class);
			paramCount = buildParameterFilter(model, paramCount, parameters, a,
					rootNode);

			if (a instanceof CompositeActor) {
				final List<TextAttribute> annotations = a
						.attributeList(TextAttribute.class);
				paramCount = buildAnnotationFilter(model, paramCount,
						annotations, rootNode);
				paramCount = addParameterFiltersForComposite(model,
						(CompositeActor) a, paramCount, rootNode);
			}
		}
		return paramCount;
	}

	/**
	 * 
	 * @param model
	 * @param paramCount
	 * @param parameters
	 * @param a
	 * @param rootNode
	 * @return
	 */
	private int buildParameterFilter(final CompositeActor model,
			final int paramCount, final List<Parameter> parameters,
			final NamedObj a, final DefaultMutableTreeNode rootNode) {
		create(model, paramCount, parameters, a, rootNode);

		return paramCount;
	}

	/**
	 * 
	 * @param model
	 * @param paramCount
	 * @param parameters
	 * @param a
	 * @param node
	 */
	private void create(final CompositeActor model, int paramCount,
			final List<Parameter> parameters, final NamedObj a,
			final DefaultMutableTreeNode rootNode) {

		// Tree level management - check on which level, the actor is displayed
		final String modelName = ModelUtils.getFullNameButWithoutModelName(
				model, a);
		// System.out.println("actor name: "+modelName);
		final StringTokenizer st = new StringTokenizer(modelName, ".");
		int actorLevel = 0;
		while (st.hasMoreElements()) {
			st.nextToken();
			actorLevel++;
		}
		if (actorLevel == 1) {
			// we restart from the highest level, reset tree
			nodesMap.clear();
		}

		// Create current actor node
		final String nodeName = modelName
				.substring(modelName.lastIndexOf(".") + 1);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
				new TreeDataObject(nodeName, true));
		nodesMap.put(actorLevel, node);
		// System.out.println("**************************level for "+modelName+" is "+actorLevel);
		// System.out.println("******Name "+nodeName);

		// all parameters for the actor
		for (int i = 0; i < parameters.size(); i++, paramCount++) {

			final Parameter p = parameters.get(i);
			final String stdName = p.getName();
			// System.out.println("adding param "+stdName);
			String alias = null;
			final boolean filterCfgKnown = getModelParameterFilterConfig()
					.getModel(model.getName()) != null;
			boolean checked = true;
			if (filterCfgKnown) {
				try {
					alias = getModelParameterFilterConfig().getModel(
							model.getName()).getFieldMapping()
							.getValueForKey(
									ModelUtils.getFullNameButWithoutModelName(
											model, p));
					checked = alias != null;
					alias = stdName.equals(alias) ? null : alias;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			final CheckableComponent cComp = new CheckableComponent(
					new JTextField(alias, 20), p, checked);
			filters.add(cComp);
			filtersSave.add(checked);

			// create a node for the parameter
			final String name = i + index + " " + p.getName();
			node = new DefaultMutableTreeNode(new TreeDataObject(name, true));
			labels.put(name, alias);

			if (actorLevel != 0) {
				// add node for param to actor node
				final DefaultMutableTreeNode actorNode = nodesMap
						.get(actorLevel);
				actorNode.add(node);
				// System.out.println("adding param to node level "+actorLevel);
			} else {
				// means that this a sequence or composite actor parameter (not
				// belong to an actor)
				rootNode.add(node);
			}

		}
		currentNode = nodesMap.get(actorLevel);

		if (actorLevel == 1) { // add current actor node to parent node
			// actor is of level 1, so add it on the root node
			rootNode.add(currentNode);
			// System.out.println("adding node "+currentNode.getLevel()+" to root "+rootNode.getLevel());
		} else if (actorLevel != 0) {
			// actor is not of level 1, so add it to its parent node
			// System.out.println("adding node of level "+actorLevel);
			final DefaultMutableTreeNode parentNode = nodesMap
					.get(actorLevel - 1);
			parentNode.add(currentNode);
		}

		index += parameters.size();
	}

	/**
	 * 
	 * @param model
	 * @param paramCount
	 * @param annotations
	 * @param e
	 * @param rootNode
	 * @return
	 */
	private int buildAnnotationFilter(final CompositeActor model,
			int paramCount, final List<TextAttribute> annotations,
			DefaultMutableTreeNode rootNode) {

		for (int i = 0; i < annotations.size(); i++, paramCount++) {
			final TextAttribute p = annotations.get(i);
			final String stdName = p.getName();
			String alias = null;

			final boolean filterCfgKnown = getModelParameterFilterConfig()
					.getModel(model.getName()) != null;
			boolean checked = true;

			// if (p.getName().equalsIgnoreCase("Annotation")) {
			// System.out.println();
			// }

			if (filterCfgKnown) {
				try {
					alias = getModelParameterFilterConfig().getModel(
							model.getName()).getFieldMapping()
							.getValueForKey(
									ModelUtils.getFullNameButWithoutModelName(
											model, p));

					checked = alias != null;
					alias = stdName.equals(alias) ? null : alias;
				} catch (final Exception e) {
					// just in case...
					e.printStackTrace();
				}
			}

			final CheckableComponent cComp = new CheckableComponent(
					new JTextField(alias, 20), p, checked);
			filters.add(cComp);
			filtersSave.add(checked);

			rootNode = new DefaultMutableTreeNode(new TreeDataObject(i + index
					+ " " + p.getName(), true));
			currentNode.add(rootNode);
		}
		index += annotations.size();
		return paramCount;
	}

	/**
	 * 
	 * @return
	 */
	public HMIDefinition getModelParameterFilterConfig() {
		return modelParameterFilterConfig;
	}

	/**
	 * return a list of selected ID
	 * 
	 * @param node
	 * @return List
	 */
	public List<String> getSelectedLeafList(final DefaultMutableTreeNode node) {
		final List<String> list = new ArrayList<String>();

		for (int i = 0; i < node.getChildCount(); i++) {
			final List<String> tmp = getSelectedLeafList((DefaultMutableTreeNode) node
					.getChildAt(i));
			list.addAll(tmp);
		}

		if (((TreeDataObject) node.getUserObject()).getStatus() == TreeDataObject.FULLY_SELECTED
				&& node.getChildCount() == 0) {
			list.add(((TreeDataObject) node.getUserObject()).getName());
		}

		// get a list of labels
		labels = treeEditor.getLabels();
		return list;
	}

	/**
	 * 
	 * @param node
	 * @return List
	 */
	public List<String> getLabelList(final DefaultMutableTreeNode node) {
		final List<String> list = new ArrayList<String>();

		for (int i = 0; i < node.getChildCount(); i++) {
			final DefaultMutableTreeNode nodeChild = (DefaultMutableTreeNode) node
					.getChildAt(i);

			for (int j = 0; j < nodeChild.getChildCount(); j++) {
				list.add(((TreeDataObject) ((DefaultMutableTreeNode) nodeChild
						.getChildAt(j)).getUserObject()).getName());
			}
		}
		return list;
	}

	/**
	 *
	 */
	public void unSelectAll() {
		final DefaultMutableTreeNode node = getRoot();
		((TreeDataObject) node.getUserObject())
				.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
		checkChild(node, false);
		repaint();
	}

	/**
	 * Check an element of the tree
	 * 
	 * @param id
	 */
	public void selectItem(final String id,
			final DefaultMutableTreeNode nodeMain) {
		final DefaultMutableTreeNode node = findNode(nodeMain, id);
		((TreeDataObject) node.getUserObject())
				.setStatus(TreeDataObject.FULLY_SELECTED);
		checkParent(node);

		repaint();
	}

	/**
	 * Uncheck an element of the tree
	 * 
	 * @param id
	 */
	public void unSelectItem(final String id) {
		final DefaultMutableTreeNode node = findNode(getRoot(), id);
		((TreeDataObject) node.getUserObject())
				.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
		final TreePath treePath = new TreePath(node.getPath());
		tree.fireTreeCollapsed(treePath);
		checkChild(node, false);
		if (node.getParent() != null) {
			checkParent((DefaultMutableTreeNode) node.getParent());
		}
		repaint();
	}

	/**
	 * updates the status of the son of a node (recursive)
	 * 
	 * @param tn
	 */
	private void checkChild(final DefaultMutableTreeNode tn, final boolean check) {
		final int childCount = tn.getChildCount();

		for (int i = 0; i < childCount; i++) {
			final TreeDataObject userObject = (TreeDataObject) ((DefaultMutableTreeNode) tn
					.getChildAt(i)).getUserObject();
			if (check) {
				userObject.setStatus(TreeDataObject.FULLY_SELECTED);
				final TreePath treePath = new TreePath(tn.getPath());
				tree.fireTreeExpanded(treePath);
			} else {
				userObject.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
				final TreePath treePath = new TreePath(tn.getPath());
				tree.fireTreeCollapsed(treePath);
			}
			if (tn.getChildAt(i).getChildCount() > 0) {
				checkChild((DefaultMutableTreeNode) tn.getChildAt(i), check);
			}
		}
	}

	/**
	 * updates the status of the parents of a node (recursive)
	 * 
	 * @param tn
	 */
	private void checkParent(final DefaultMutableTreeNode tn) {
		final int childCount = tn.getChildCount();

		final TreeDataObject userObject = (TreeDataObject) tn.getUserObject();

		int nbCheck = 0;
		int nbCheckP = 0;

		for (int i = 0; i < childCount; i++) {
			final TreeDataObject userObjectChild = (TreeDataObject) ((DefaultMutableTreeNode) tn
					.getChildAt(i)).getUserObject();
			switch (userObjectChild.getStatus()) {
			case TreeDataObject.FULLY_NOT_SELECTED:
				break;
			case TreeDataObject.FULLY_SELECTED:
				nbCheck++;
				break;
			case TreeDataObject.PARTIAL_SELECTED:
				nbCheckP++;
				break;
			}
		}

		if (nbCheck == childCount) {
			userObject.setStatus(TreeDataObject.FULLY_SELECTED);
		} else {
			if (nbCheck == 0 && nbCheckP == 0) {
				userObject.setStatus(TreeDataObject.FULLY_NOT_SELECTED);
			} else {
				userObject.setStatus(TreeDataObject.PARTIAL_SELECTED);
			}
		}
		if (tn.getParent() != null) {
			checkParent((DefaultMutableTreeNode) tn.getParent());
		}
	}

	/**
	 * Return the node corresponding to the id parameter from a node Return null
	 * if not found
	 * 
	 * @param node
	 * @param id
	 * @return DefaultMutableTreeNode
	 */
	private DefaultMutableTreeNode findNode(final DefaultMutableTreeNode node,
			final String id) {
		if (((TreeDataObject) node.getUserObject()).getName().equals(id)) {
			return node;
		} else {
			for (int i = 0; i < node.getChildCount(); i++) {
				final DefaultMutableTreeNode tmp = findNode(
						(DefaultMutableTreeNode) node.getChildAt(i), id);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}

	/**
	 * return the node root of the tree
	 * 
	 * @return
	 */
	public DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) tree.getModel().getRoot();
	}

	/**
	 *
	 */
	public void disableCheck() {
		final DefaultMutableTreeNode node = getRoot();
		((TreeDataObject) node.getUserObject())
				.setCheckable(TreeDataObject.NOT_CHECKABLE);
		checkableChild(node, TreeDataObject.NOT_CHECKABLE);
		repaint();
	}

	/**
	 *
	 */
	public void enableCheck() {
		final DefaultMutableTreeNode node = getRoot();
		((TreeDataObject) node.getUserObject())
				.setCheckable(TreeDataObject.CHECKABLE);
		checkableChild(node, TreeDataObject.CHECKABLE);
		repaint();
	}

	/**
	 * 
	 * @param tn
	 * @param checkable
	 */
	private void checkableChild(final DefaultMutableTreeNode tn,
			final boolean checkable) {
		final int childCount = tn.getChildCount();

		for (int i = 0; i < childCount; i++) {
			final TreeDataObject userObject = (TreeDataObject) ((DefaultMutableTreeNode) tn
					.getChildAt(i)).getUserObject();
			if (checkable) {
				userObject.setCheckable(TreeDataObject.CHECKABLE);
				final TreePath treePath = new TreePath(tn.getPath());
				tree.fireTreeExpanded(treePath);
			} else {
				userObject.setCheckable(TreeDataObject.NOT_CHECKABLE);
				final TreePath treePath = new TreePath(tn.getPath());
				tree.fireTreeCollapsed(treePath);
			}
			if (tn.getChildAt(i).getChildCount() > 0) {
				checkableChild((DefaultMutableTreeNode) tn.getChildAt(i),
						checkable);
			}
		}
	}

	/**
	 * 
	 * @param tn
	 */
	private void checkableParent(final DefaultMutableTreeNode tn) {

		final int childCount = tn.getChildCount();
		final TreeDataObject userObject = (TreeDataObject) tn.getUserObject();
		int nbCheckable = 0;

		for (int i = 0; i < childCount; i++) {
			final TreeDataObject userObjectChild = (TreeDataObject) ((DefaultMutableTreeNode) tn
					.getChildAt(i)).getUserObject();
			if (userObjectChild.isCheckable()) {
				nbCheckable++;
			}
		}

		if (nbCheckable > 0) {
			userObject.setCheckable(TreeDataObject.CHECKABLE);
		} else {
			userObject.setCheckable(TreeDataObject.NOT_CHECKABLE);
		}
		if (tn.getParent() != null) {
			checkableParent((DefaultMutableTreeNode) tn.getParent());
		}
	}

	/**
	 * 
	 * @param id
	 */
	public void setItemCheckable(final String id) {
		final DefaultMutableTreeNode node = findNode(getRoot(), id);
		((TreeDataObject) node.getUserObject())
				.setCheckable(TreeDataObject.CHECKABLE);
		checkableChild(node, TreeDataObject.CHECKABLE);
		if (node.getParent() != null) {
			checkableParent((DefaultMutableTreeNode) node.getParent());
		}
		repaint();
	}

	/**
	 * 
	 * @param id
	 */
	public void setItemNotCheckable(final String id) {
		final DefaultMutableTreeNode node = findNode(getRoot(), id);
		((TreeDataObject) node.getUserObject())
				.setCheckable(TreeDataObject.NOT_CHECKABLE);
		checkableChild(node, TreeDataObject.NOT_CHECKABLE);
		if (node.getParent() != null) {
			checkableParent((DefaultMutableTreeNode) node.getParent());
		}
		repaint();
	}

	/**
	 * 
	 * @return
	 */
	public List<Component> getFilters() {
		return filters;
	}

	/**
	 * 
	 * @param filters
	 */
	public void setFilters(final List<Component> filters) {
		this.filters = filters;
	}

	/**
	 * 
	 * @return
	 */
	public int getParamCount() {
		return paramCount;
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String, String> getLabels() {
		return labels;
	}

	/**
	 *
	 */
	public void setLabels(final HashMap<String, String> labels) {
		this.labels = labels;
	}

	// --------Search management----------

	/**
	 * 
	 * @param bindings
	 */
	private void bindPatternModel() {
		patternModel = new PatternModel();

		// -------- create control bindings------------
		searchText.addKeyListener(new KeyListener() {

			public void keyPressed(final KeyEvent e) {
				patternModel.setRawText(searchText.getText());
			}

			public void keyReleased(final KeyEvent e) {
				patternModel.setRawText(searchText.getText());
			}

			public void keyTyped(final KeyEvent e) {
				patternModel.setRawText(searchText.getText());
			}
		});

		// ------- wire control value changes to presentation changes------

		patternModel.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(final PropertyChangeEvent evt) {
				if ("pattern".equals(evt.getPropertyName())) {
					updatePattern((Pattern) evt.getNewValue());
				}
			}
		});
	}

	/**
	 * called after pattern changed.
	 * 
	 * @param pattern
	 */
	protected void updatePattern(final Pattern pattern) {
		final HighlightPredicate predicate = new SearchPredicate(pattern, -1,
				-1);
		matchHighlighter.setHighlightPredicate(predicate);
		tree.expandAll();

		if (searchText.getText().equals("")) {
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.collapseRow(i + 1);
			}
		}
	}

	/**
	 * @return the tree
	 */
	public JXTree getTree() {
		return tree;
	}
}