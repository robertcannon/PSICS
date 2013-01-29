package org.psics.read;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.be.FileSourced;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.FolderPath;
import org.psics.quantity.annotation.LibraryPath;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.SubComponent;

public class ResourceMap {

	Object root;
	ModelSource modelSource;
	HashMap<String, ProxMap> globalHM;
	int nanon = 0;
	ArrayList<RootProxMap> resolveQueue = new ArrayList<RootProxMap>();

	ArrayList<String> libPaths = new ArrayList<String>();


	public ResourceMap(Object rt) {
		root = rt;
		globalHM = new HashMap<String, ProxMap>();

		if (root == null) {
			E.error("null root creating resource map?");
		}

		RootProxMap rpm = new RootProxMap(root);
		proxify(rpm);
		resolveQueue.add(rpm);

	}


	public void resolve(ModelSource ms) {
		modelSource = ms;
		for (String s : libPaths) {
			ms.addLibrary(null, s);
		}
		while (resolveQueue.size() > 0) {
			RootProxMap toresolve = resolveQueue.get(0);
			resolveQueue.remove(0);
			resolveOne(toresolve);
		}
	}


	private void proxify(RootProxMap rpm) {
		recProxify(rpm, rpm.getSubList());
	}


	private void resolveOne(RootProxMap rpm) {
		ArrayList<ProxMap> subPMs = rpm.getSubList();
		while (subPMs.size() > 0) {
			ProxMap apm = subPMs.get(0);
			subPMs.remove(0);
				
			// E.info("resolving " + apm + " " + apm.peer);
			
			if (apm.resolved()) {

			} else {
				resolveLocal(apm);
			}
		}
	}

	  @SuppressWarnings("unchecked")
	private void recProxify(ProxMap pm, ArrayList<ProxMap> all) {
		all.add(pm);

		boolean gotID = false;

		Object obj = pm.getPeer();

		// E.info("rec proxify " + obj);

		for (Field fld : obj.getClass().getFields()) {
			Annotation[] aa = fld.getAnnotations();
			if (aa != null && aa.length > 0) {
				for (Annotation ant : aa) {
					if (ant instanceof Container
							&& fld.getType().equals(ArrayList.class)) {
						try {
							ArrayList<? extends Object> ale = (ArrayList<? extends Object>) (fld.get(obj));
							for (Object listitem : ale) {
								if (ale == null) {
									E.error("null item in list for field " + fld);
								} else {
									recProxify(new ProxMap(listitem, pm), all);
								}
							}

						} catch (Exception ex) {
							E.error("cant get list field in " + obj.getClass().getName()
									+ fld + " \n" + ex);
						}

					} else if (ant instanceof SubComponent) {
						// E.info("subcomponent annotation for " + fld + " on " + obj);
						try {
							Object sub = fld.get(obj);
							if (sub != null) {
								recProxify(new ProxMap(sub, pm), all);
							}
						} catch (Exception ex) {
							E.info("exceptin getting subcomponent? " + ant + " " + fld);
							// ignora if not populated?
						}

					} else if (ant instanceof Identifier
							&& fld.getType().equals(String.class)) {
						try {
							String sid = (String) (fld.get(obj));
							pm.setPeerID(sid);
							if (sid != null && sid.trim().length() > 0) {
								if (pm.getParent() != null) {
									pm.getParent().put(sid, pm);
									// E.info("put id in parent " + sid + " " + pm.getParent());
								} else {
									// E.info("put id global " + sid);
									globalHM.put(sid, pm);
								}
								gotID = true;
							} else {
								// E.info("no id in identified cpt " + obj);
							}

						} catch (Exception ex) {
							E.error("cant get id field? " + ex);
						}

					} else if (ant instanceof LibraryPath) {
						try {
							String sid = (String) (fld.get(obj));
							libPaths.add(sid);

						} catch (Exception ex) {
							E.error("cant get lib path? " + ex);
						}
					}
				}
			}
		}
		if (!gotID) {
			nanon += 1;
			globalHM.put("anon" + nanon, pm);
		}
	}




	private void resolveLocal(ProxMap pm) {
		Object obj = pm.getPeer();

		if (obj instanceof FileSourced) {
			modelSource.populateFileSourced((FileSourced)obj);
		}


		for (Field fld : obj.getClass().getFields()) {
			Annotation[] aa = fld.getAnnotations();

			if (aa != null && aa.length > 0) {
				for (Annotation ant : aa) {
					if (ant instanceof ReferenceByIdentifier
							&& fld.getType().equals(String.class)) {
						// E.info("time to deref " + fld.getName());
						ReferenceByIdentifier rbi = (ReferenceByIdentifier) ant;

						try {
							String ref = (String) fld.get(obj);
							if (ref != null && ref.trim().length() > 0) {
								if (rbi.location().equals(Location.local)) {

									if (pm.has(ref)) {
										setReferenceTarget(obj, fld.getName(),
												pm.get(ref).getPeer());
									} else {
										E.error("no local target " + ref + " " + rbi.tag());
									}

								} else if (rbi.location().equals(Location.indirect)) {
									// indirect name isn't great - these are things that live inside other
									// objects that have already been loaded
									boolean got = false;
									for (ProxMap rpm : globalHM.values()) {
										if (rpm.has(ref)) {
											setReferenceTarget(obj, fld.getName(), rpm.get(ref).getPeer());
											got = true;
											break;
										}
									}
									if (!got) {
										E.oneLineWarning("cant find indirect target " + ref);
									} else {
									//	E.info("resolved indirect ref " + ref);
									}



								} else  if (rbi.location().equals(Location.global)) {
									if (globalHM.containsKey(ref)) {
										setReferenceTarget(obj, fld.getName(),
												globalHM.get(ref).getPeer());

									} else {
										ProxMap tgtpm = findGlobalTarget(ref);
										if (tgtpm != null) {
											setReferenceTarget(obj, fld.getName(), tgtpm.getPeer());
										}
									}
								}
							}

						} catch (Exception ex) {
							E.error("Error handling reference field " + fld + " " + ex);
							ex.printStackTrace();
						}
					}
				}
			}
		}

	}

	private ProxMap findGlobalTarget(String ref) {
		ProxMap ret = null;
		if (modelSource.canGet(ref)) {
			Object obj = modelSource.get(ref);
			if (obj == null) {
				E.error("Map knows about " + ref + " but the object is null");
			}
			RootProxMap rpm = new RootProxMap(obj);
			proxify(rpm);
			resolveQueue.add(rpm);
			ret = rpm;

		} else {
			E.error("reference to " + ref + " - cant find target  " +
					modelSource.listItems());
		}

		return ret;
	}

	private void setReferenceTarget(Object obj, String name, Object value) {
		try {
			Field f = obj.getClass().getField("r_" + name);
			f.set(obj, value);

		} catch (Exception ex) {
			E.linkToError("cant set ref target " + name + " on " + obj + " to "
					+ value, obj);
		}

	}

}
