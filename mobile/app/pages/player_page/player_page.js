import loggerCreator from "app/utils/logger";
var moduleLogger = loggerCreator("player_page");

import React, { Component } from "react";
import { StyleSheet, View, Image } from "react-native";
import BackHandler from "app/utils/back_handler/back_handler";
import { observer } from "mobx-react";

import Icon from "app/shared_components/icon";
import { colors, fontSizes } from "app/styles/styles";
import BigText from "app/shared_components/text/big_text";
import PlayerContextMenu from "./player_context_menu";
import navigator from "app/stores/navigator/navigator";
import player from "app/stores/player/player";
import Rating from "./rating";
import AlbumArt from "./album_art";
import PlayerControls from "./player_controls";
import PlayerLoadingSpinner from "./player_loading_spinner";

const styles = StyleSheet.create({
  container: {
    flex: 1,
    // remove width and height to override fixed static size
    width: null,
    height: null,
    alignItems: "center",
    alignSelf: "stretch",
  },
  // Playlist name
  playlistNameView: {
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "stretch",
    marginBottom: 10,
    paddingTop: 5,
    paddingLeft: 5,
  },
  playlistIcon: {
    color: colors.SEMI_WHITE,
    marginRight: 5,
  },
  // Sections
  albumArt: {
    marginBottom: 20,
  },
  rating: {
    marginBottom: 20,
    paddingHorizontal: 10,
  },
  songDetails: {
    alignItems: "center",
    marginBottom: 20,
  },
  // Names (artist, title, album)
  nameText: {
    marginBottom: 2,
  },
  artistText: {
    color: colors.CYAN_BRIGHT,
  },
});

@observer
export default class PlayerPage extends Component {
  componentWillMount() {
    let logger = loggerCreator("componentWillMount", moduleLogger);
    logger.info(`playlist: ${this.props.playlistName}`);

    BackHandler.addEventListener("hardwareBackPress", () => this.onPressHardwareBack());
  }

  componentDidMount() {
    loggerCreator("componentDidMount", moduleLogger);
  }

  componentWillUnmount() {
    loggerCreator("componentWillUnmount", moduleLogger);
  }

  onPressHardwareBack() {
    loggerCreator("hardwareBackPress", moduleLogger);
    player.pause();
    navigator.navigateToPlaylistCollection();
    return true;
  }

  render() {
    let logger = loggerCreator("render", moduleLogger);

    const song = player.song;
    logger.info(`rendering song: ${song && song.toString()}`);

    return (
      <View style={styles.container}>
        {!player.isLoading
          ? <View>
              {/* Album art */}
              <AlbumArt style={[styles.albumArt]} song={song} />
              {/* Ratings */}
              <View style={styles.rating}>
                <Rating song={song} />
                <PlayerContextMenu song={song} />
              </View>
              {/* Names */}
              <View style={styles.songDetails}>
                <BigText style={[styles.nameText]}>{`${song.title}`}</BigText>
                <BigText style={[styles.nameText, styles.artistText]}>{`${song.artist}`}</BigText>
                <BigText style={[styles.nameText]}>{`${song.album}`}</BigText>
              </View>
              {/* Controls */}
              <PlayerControls />
            </View>
          : <PlayerLoadingSpinner song={song} />}
      </View>
    );
  }
}

PlayerPage.propTypes = {
  playlistName: React.PropTypes.string.isRequired,
};
