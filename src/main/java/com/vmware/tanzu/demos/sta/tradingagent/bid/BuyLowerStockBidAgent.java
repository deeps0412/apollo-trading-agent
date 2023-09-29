package com.vmware.tanzu.demos.sta.tradingagent.bid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.agent.strategy", havingValue = "buy-lower-stock")
class BuyLowerStockBidAgent implements BidAgent {
    private final Logger logger = LoggerFactory.getLogger(BuyLowerStockBidAgent.class);

    @Autowired
    private RestTemplate client;

    @Override
    public List<BidAgentRequest> execute(Context ctx) {
        // Sort input stocks against price.
        final List<Stock> sortedStocks = new ArrayList<>(ctx.stocks());
        sortedStocks.sort(Comparator.comparing(Stock::price));

        final Stock lowerStock = sortedStocks.get(0);
        logger.info("Found a stock with the lower value: {}", lowerStock.symbol());
        return List.of(new BidAgentRequest(lowerStock.symbol(), 100));
    }

    private List<BidAgentRequest> getStockValues(List<Stock> sortedStocks){
        List<BidAgentRequest> list = new ArrayList<>();
        for(Stock stock: sortedStocks){
            var history = client.getForObject("/api/v1/stocks/"+stock.symbol()+"/values", History[].class);
            if(history[history.length-2].price().compareTo(history[history.length-1].price())<0){
                logger.info("buy : {}",stock.symbol());
                list.add(new BidAgentRequest(stock.symbol(),100));
            }else{
                logger.info("sell : {}",stock.symbol());
                list.add(new BidAgentRequest(stock.symbol(),-stock.shares()));
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return "BUY_LOWER_STOCK";
    }
}
