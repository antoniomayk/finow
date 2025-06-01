{
  description = "finow";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-25.05";
  };

  outputs = {nixpkgs, ...}: let
    system = "x86_64-linux";
    pkgs = import nixpkgs { inherit system; };
  in {
    devShells.${system}.default = pkgs.mkShell {
      packages = [
      	pkgs.jdk24
      	pkgs.maven
      ];
      shellHook = ''
        export SHELL=/run/current-system/sw/bin/bash
      '';
    };
  };
}
