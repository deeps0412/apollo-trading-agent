package com.vmware.tanzu.demos.sta.tradingagent.bid;

import java.math.BigDecimal;

public record History (String time, BigDecimal price) {
}
