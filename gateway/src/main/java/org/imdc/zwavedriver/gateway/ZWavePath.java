package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ZWavePath {
    public static final String SEPARATOR = "/";

    public enum Marker {
        HOME("Homes"),
        NODE("Nodes"),
        COMMANDCLASS("CommandClasses");

        private String path;

        Marker(String path) {
            this.path = path;
        }
    }

    private Integer homeId;
    private Byte nodeId;
    private Byte commandClass;

    private ArrayList<String> homePaths;
    private ArrayList<String> nodePaths;
    private ArrayList<String> commandClassPaths;

    private Marker lastMarker;

    public ZWavePath(Integer homeId) {
        this(homeId, null);
    }

    public ZWavePath(Integer homeId, Byte nodeId) {
        this(homeId, nodeId, null);
    }

    public ZWavePath(Integer homeId, Byte nodeId, Byte commandClass) {
        this.homeId = homeId;
        this.nodeId = nodeId;
        this.commandClass = commandClass;

        if (commandClass != null) {
            lastMarker = Marker.COMMANDCLASS;
        } else if (nodeId != null) {
            lastMarker = Marker.NODE;
        } else {
            lastMarker = Marker.HOME;
        }
    }

    public ZWavePath(String path) {
        String[] parts = path.split(SEPARATOR);
        Marker currentMarker = null;
        int currentMarkerPosition = 0;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (part.equals(Marker.HOME.path)) {
                currentMarker = Marker.HOME;
                currentMarkerPosition = i;
            } else if (part.equals(Marker.NODE.path)) {
                currentMarker = Marker.NODE;
                currentMarkerPosition = i;
            } else if (part.equals(Marker.COMMANDCLASS.path)) {
                currentMarker = Marker.COMMANDCLASS;
                currentMarkerPosition = i;
            }

            if (currentMarker == Marker.HOME) {
                lastMarker = Marker.HOME;
                if (i == (currentMarkerPosition + 1)) {
                    homeId = Hex.stringToInt(part);
                } else if (i > (currentMarkerPosition + 1)) {
                    addHomePath(part);
                }
            } else if (currentMarker == Marker.NODE) {
                lastMarker = Marker.NODE;
                if (i == (currentMarkerPosition + 1)) {
                    nodeId = Hex.stringToByte(part);
                } else if (i > (currentMarkerPosition + 1)) {
                    addNodePath(part);
                }
            } else if (currentMarker == Marker.COMMANDCLASS) {
                lastMarker = Marker.COMMANDCLASS;
                if (i == (currentMarkerPosition + 1)) {
                    commandClass = Hex.stringToByte(part);
                } else if (i > (currentMarkerPosition + 1)) {
                    addCommandClassPath(part);
                }
            }
        }
    }

    public Integer getHomeId() {
        return homeId;
    }

    public boolean isNodeSet(){
        return nodeId != null;
    }

    public Byte getNodeId() {
        return nodeId;
    }

    public Byte getCommandClass() {
        return commandClass;
    }

    public boolean isCommandClassSet(){
        return commandClass != null;
    }

    public CommandClasses getCommandClassObj() throws DecoderException {
        return CommandClasses.from(commandClass);
    }

    public void setHomeId(Integer homeId) {
        this.homeId = homeId;
        lastMarker = Marker.HOME;
    }

    public ZWavePath tag(String... path) {
        clear();
        for (String p : path) {
            add(p);
        }
        return this;
    }

    public void setHomePaths(ArrayList<String> homePaths) {
        this.homePaths = homePaths;
    }

    public void setNodePaths(ArrayList<String> nodePaths) {
        this.nodePaths = nodePaths;
    }

    public void setCommandClassPaths(ArrayList<String> commandClassPaths) {
        this.commandClassPaths = commandClassPaths;
    }

    public ZWavePath add(String path) {
        return addPath(lastMarker, path);
    }

    public ZWavePath addHomePath(String path) {
        return addPath(Marker.HOME, path);
    }

    public ZWavePath addNodePath(String path) {
        return addPath(Marker.NODE, path);
    }

    public ZWavePath addCommandClassPath(String path) {
        return addPath(Marker.COMMANDCLASS, path);
    }

    private ZWavePath addPath(Marker marker, String path) {
        ArrayList<String> whichList = null;

        if (marker == Marker.HOME) {
            whichList = homePaths;
        } else if (marker == Marker.NODE) {
            whichList = nodePaths;
        } else if (marker == Marker.COMMANDCLASS) {
            whichList = commandClassPaths;
        }

        if (whichList == null) {
            whichList = new ArrayList<>();

            if (marker == Marker.HOME) {
                homePaths = whichList;
            } else if (marker == Marker.NODE) {
                nodePaths = whichList;
            } else if (marker == Marker.COMMANDCLASS) {
                commandClassPaths = whichList;
            }
        }

        whichList.add(path);

        return this;
    }

    public String get(int position) {
        return get(lastMarker, position);
    }

    public String get(Marker marker, int position) {
        List<String> whichList = null;

        if (marker == Marker.HOME) {
            whichList = homePaths;
        } else if (marker == Marker.NODE) {
            whichList = nodePaths;
        } else if (marker == Marker.COMMANDCLASS) {
            whichList = commandClassPaths;
        }

        if (whichList != null && whichList.size() > position) {
            return whichList.get(position);
        }

        return null;
    }

    public ZWavePath clear() {
        if (homePaths != null) {
            homePaths.clear();
        }

        if (nodePaths != null) {
            nodePaths.clear();
        }

        if (commandClassPaths != null) {
            commandClassPaths.clear();
        }

        return this;
    }

    public void setNodeId(Byte nodeId) {
        this.nodeId = nodeId;
        lastMarker = Marker.NODE;
    }

    public void setCommandClass(Byte commandClass) {
        this.commandClass = commandClass;
        lastMarker = Marker.COMMANDCLASS;
    }

    public String getHomeIdHex() {
        return Hex.asString(homeId);
    }

    public String getNodeIdHex() {
        return Hex.asString(nodeId);
    }

    public String getCommandClassHex() {
        return Hex.asString(commandClass);
    }

    public String getHomePath() {
        return getHomePath(false);
    }

    public String getHomePath(boolean showPaths) {
        if (homeId == null) {
            return null;
        }
        return buildPath(Marker.HOME.path, getHomeIdHex(), showPaths ? combinePaths(homePaths) : null);
    }

    public String getNodePath() {
        return getNodePath(false);
    }

    public String getNodePath(boolean showPaths) {
        if (nodeId == null) {
            return getHomePath(showPaths);
        }
        return buildPath(getHomePath(showPaths), Marker.NODE.path, getNodeIdHex(), showPaths ? combinePaths(nodePaths) : null);
    }

    public String getCommandClassPath() {
        return getCommandClassPath(false);
    }

    public String getCommandClassPath(boolean showPaths) {
        if (commandClass == null) {
            return getNodePath(showPaths);
        }
        return buildPath(getNodePath(showPaths), Marker.COMMANDCLASS.path, getCommandClassHex(), showPaths ? combinePaths(commandClassPaths) : null);
    }

    public String getFullPath() {
        return getCommandClassPath(true);
    }

    private static String combinePaths(List<String> paths) {
        StringBuilder builder = new StringBuilder();
        if (paths != null && paths.size() > 0) {
            for (int i = 0; i < paths.size(); i++) {
                if (paths.get(i) != null) {
                    builder.append(paths.get(i));
                    builder.append(SEPARATOR);
                }
            }

            return StringUtils.removeEnd(builder.toString(), SEPARATOR);
        }

        return null;
    }

    private static String buildPath(String... path) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            if (path[i] != null) {
                builder.append(path[i]);
                builder.append(SEPARATOR);
            }
        }

        return StringUtils.removeEnd(builder.toString(), SEPARATOR);
    }

    public ZWavePath copy() {
        ZWavePath clone = new ZWavePath(getHomeId(), getNodeId(), getCommandClass());
        ArrayList<String> cloneHomePaths = null;
        ArrayList<String> cloneNodePaths = null;
        ArrayList<String> cloneCommandClassPaths = null;

        if (homePaths != null && homePaths.size() > 0) {
            cloneHomePaths = (ArrayList<String>) homePaths.clone();
        }

        if (nodePaths != null && nodePaths.size() > 0) {
            cloneNodePaths = (ArrayList<String>) nodePaths.clone();
        }

        if (commandClassPaths != null && commandClassPaths.size() > 0) {
            cloneCommandClassPaths = (ArrayList<String>) commandClassPaths.clone();
        }

        clone.setHomePaths(cloneHomePaths);
        clone.setNodePaths(cloneNodePaths);
        clone.setCommandClassPaths(cloneCommandClassPaths);
        return clone;
    }
}
