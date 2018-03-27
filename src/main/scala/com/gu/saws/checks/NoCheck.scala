package com.gu.saws.checks

import com.typesafe.scalalogging.StrictLogging

case class NoCheck() extends StrictLogging {

  def check(): Boolean = {
    logger.info("Signalling success immediately")
    true
  }

}

