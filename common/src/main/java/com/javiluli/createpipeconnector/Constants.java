package com.javiluli.createpipeconnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants used across all loaders and shared code.
 */
public class Constants {
	/**
	 * The mod identifier used by every loader entrypoint and metadata file.
	 */
	public static final String MOD_ID = "createpipeconnector";

	/**
	 * The display name of the mod used for logging.
	 */
	public static final String MOD_NAME = "Create: Pipe Connector";

	/**
	 * Shared logger instance for the mod.
	 */
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}
