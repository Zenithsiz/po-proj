package ggc.core;

import java.io.Serializable;

/// Partner
public class Partner implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_03_12L;

	/// Name
	private String _name;

	/// Address
	private String _address;

	/// Id
	private String _id;

	/// Status
	private PartnerStatus _status;

	/// Points
	private double _points;

	public Partner(String name, String address, String id) {
		_name = name;
		_address = address;
		_id = id;
		_status = PartnerStatus.Normal;
		_points = 0.0;
	}

	/// Returns the partner's id
	public String getId() {
		return _id;
	}

	/// Partner status
	public enum PartnerStatus {
		Normal, Selection, Elite,
	}
}
