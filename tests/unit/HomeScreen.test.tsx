import 'react-native';
import React from 'react';
import renderer from 'react-test-renderer';

import {HomeScreen} from '../../src/screens/Home/HomeScreen';

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({
    navigate: jest.fn(),
    goBack: jest.fn(),
  }),
}));

describe('HomeScreen', () => {
  it('renders correctly', () => {
    const tree = renderer.create(<HomeScreen />).toJSON();
    expect(tree).toBeTruthy();
  });

  it('displays the app title', () => {
    const instance = renderer.create(<HomeScreen />);
    const root = instance.root;
    const texts = root.findAllByType('Text');
    const titleText = texts.find(t => t.props.children === 'CelUsado');
    expect(titleText).toBeTruthy();
  });

  it('has navigation buttons', () => {
    const instance = renderer.create(<HomeScreen />);
    const root = instance.root;
    const texts = root.findAllByType('Text');
    const buttonTexts = texts.filter(t =>
      ['Analizar equipo', 'Info. dispositivo', 'Aplicaciones', 'Informe'].includes(
        t.props.children as string,
      ),
    );
    expect(buttonTexts.length).toBe(4);
  });
});
